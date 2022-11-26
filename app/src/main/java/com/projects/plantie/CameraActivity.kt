/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.projects.plantie

//import androidx.camera.core.ImageCapture
//import org.tensorflow.lite.examples.plantie.databinding.ActivityMainBinding
//import com.projects.plantie.Location.getLocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.projects.plantie.ui.RecognitionAdapter
import com.projects.plantie.util.YuvToRgbConverter
import com.projects.plantie.viewmodel.Recognition
import com.projects.plantie.viewmodel.RecognitionListViewModel
import org.tensorflow.lite.examples.plantie.R
import org.tensorflow.lite.examples.plantie.ml.FlowerModel
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.model.Model
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors


// Constants
private var flower_label: String = ""    // TODO global variable flower label
//TODO current_flower_label
private const val MAX_RESULT_DISPLAY = 3 // Maximum number of results displayed
private var takePhotoButton: ImageButton? = null
private var getLocationButton: Button? = null

// Listener for the result of the ImageAnalyzer
typealias RecognitionListener = (recognition: List<Recognition>) -> Unit

/**
 * Main entry point into TensorFlow Lite Classifier
 */
class CameraActivity : AppCompatActivity() {

//    private lateinit var viewBinding: ActivityMainBinding

    // CameraX variables
    private lateinit var preview: Preview // Preview use case, fast, responsive view of the camera
//    private lateinit var imageAnalyzer: ImageAnalysis // Analysis use case, for running ML code
    private var imageCapture: ImageCapture? = null
    private lateinit var camera: Camera
    private val cameraExecutor = Executors.newSingleThreadExecutor()

//    Location Manager
    private lateinit var locationManager: LocationManager
    private val locationPermissionCode = 2
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Views attachment
    private val resultRecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recognitionResults) // Display the result of analysis
    }
    private val viewFinder by lazy {
        findViewById<PreviewView>(R.id.viewFinder) // Display the preview image from Camera
    }

    // Contains the recognition result. Since  it is a viewModel, it will survive screen rotations
    private val recogViewModel: RecognitionListViewModel by viewModels()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this!!)
//        viewBinding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(viewBinding.root)
        setContentView(R.layout.activity_camera)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        // Initialising the resultRecyclerView and its linked viewAdaptor
        val viewAdapter = RecognitionAdapter(this)
        resultRecyclerView.adapter = viewAdapter

        // Disable recycler view animation to reduce flickering, otherwise items can move, fade in
        // and out as the list change
        resultRecyclerView.itemAnimator = null

        // Attach an observer on the LiveData field of recognitionList
        // This will notify the recycler view to update every time when a new list is set on the
        // LiveData field of recognitionList.
        recogViewModel.recognitionList.observe(this,
            Observer {
                viewAdapter.submitList(it)
            }
        )

        // take photo button
        takePhotoButton = findViewById<ImageButton>(R.id.image_capture_button)
        takePhotoButton!!.setOnClickListener{ takePhoto() }

        // get Location Button for testing
        // TODO delete the button after testing
        getLocationButton = findViewById<Button>(R.id.get_location_button)
        getLocationButton!!.setOnClickListener{ getLastKnownLocation() }
//        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

    }

    /**
     * Check all permissions are granted - use for Camera permission in this example.
     */
    private fun allPermissionsGranted(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * This gets called after the Camera permission pop up is shown.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Exit the app if permission is not granted
                // Best practice is to explain and offer a chance to re-request but this is out of
                // scope in this sample. More details:
                // https://developer.android.com/training/permissions/usage-notes
                Toast.makeText(
                    this,
                    getString(R.string.permission_deny_text),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    /**
     * Start the Camera which involves:
     *
     * 1. Initialising the preview use case
     * 2. Initialising the image analyser use case
     * 3. Attach both to the lifecycle of this activity
     * 4. Pipe the output of the preview object to the PreviewView on the screen
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                // This sets the ideal size for the image to be analyse, CameraX will choose the
                // the most suitable resolution which may not be exactly the same or hold the same
                // aspect ratio
                .setTargetResolution(Size(224, 224))
                // How the Image Analyser should pipe in input, 1. every frame but drop no frame, or
                // 2. go to the latest frame and may drop some frame. The default is 2.
                // STRATEGY_KEEP_ONLY_LATEST. The following line is optional, kept here for clarity
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase: ImageAnalysis ->
                    analysisUseCase.setAnalyzer(cameraExecutor, ImageAnalyzer(this) { items ->
                        // updating the list of recognised objects
                        recogViewModel.updateData(items)
                    })
                }

            // Select camera, back is the default. If it is not available, choose front camera
            val cameraSelector =
                if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA))
                    CameraSelector.DEFAULT_BACK_CAMERA else CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera - try to bind everything at once and CameraX will find
                // the best combination.
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer
                )

                // Attach the preview to preview view, aka View Finder
                preview.setSurfaceProvider(viewFinder.surfaceProvider)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto(){
        println("take photo")
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
//                println("change path?")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Plantie-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()
        println(outputOptions)

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    println("failed to take photo")
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults){
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    println("saving photo")
                    val photo_path = output.savedUri
                    uploadToDatabase(photo_path, name)
                }
            }
        )
    }

    fun getLastKnownLocation(): DoubleArray {
        println("get LastKnownLocation")
        var gps = DoubleArray(2)
        gps[0] = 0.0
        gps[1] = 0.1
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {

                    gps[0] = location.latitude
                    gps[1] = location.longitude
                    println(gps[0])
                    println(gps[1])
                    Log.e("gpsLat",gps[0].toString())
                    Log.e("gpsLong",gps[1].toString())


                }
                else {
                    println("no location find")
                }
            }
        return gps
    }

    //get real path!!!!!!!
    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }



//    TODO recognise the photo and pass the parameters to upload
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun uploadToDatabase(photo_path: Uri?, time: String?){
        println("Uploading to DB")
        //val time = SimpleDateFormat(FILENAME_FORMAT).format(System.currentTimeMillis())
        val gps = getLastKnownLocation()
        val lat = gps[0]
        val long = gps[1]
        val label = flower_label  // TODO global variable flower label

        //val model = FlowerModel.newInstance(applicationContext)
        // Creates inputs for reference.
        //val image = TensorImage.fromBitmap(bitmap)
        // Runs model inference and gets result.
        //val outputs = model.process(image)
        //val probability = outputs.probabilityAsCategoryList
        // Releases model resources if no longer used.
        //model.close()


        val realpath = photo_path?.let { getRealPathFromURI(it) }
        Log.i("uploadToDatabase", realpath.toString())
        val options = StorageUploadFileOptions.builder()
            .accessLevel(StorageAccessLevel.PRIVATE)
            .build()

        if (realpath != null) {
            if (time != null) {
                Amplify.Storage.uploadFile(time, File(realpath), options,
                    {
                        Log.i("AmplifyS3", "Successfully uploaded: ${it.key}")
                        var msg = "Image uploaded"
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    {
                        Log.e("AmplifyS3", "Upload failed", it)
                        Log.i("S3Exception", it.message.toString())
                        Log.i("S3Exception", it.cause.toString())
                        var msg = ""
                        msg = when (it.message){
                            "Something went wrong with your AWS S3 Storage upload file operation" -> "Sync with cloud failed. Please login if you still haven't"
                            else -> "Fatal error"
                        }
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        //TODO re-ana
        //var pic = File(realpath)
        //val picana= ImageAnalyzer(applicationContext, )


        //exif
        //val inputStream = photo_path?.let { contentResolver.openInputStream(it) }
            //val pic = File(photo_path?.path)
            //val exif = ExifInterface(path2.toString())
        //val exif = inputStream?.let { ExifInterface(it) }
        //val timme = exif?.getAttribute("TAG_DATETIME")
        //val lat: Double = exif.getAttributeDouble("TAG_GPS_DEST_LATITUDE",-1)
        //val lat= exif?.getAttribute("TAG_GPS_DEST_LATITUDE")
        //val long=exif.getAttribute("TAG_GPS_DEST_LONGITUDE")

//        upload(label, time, long, lat, realpath)

        //val options = StorageListOptions.builder()
        //    .accessLevel(StorageAccessLevel.PRIVATE)
        //    .targetIdentityId("otherUserID")
        //    .build()
        //
        //Amplify.Storage.list("", options,
        //    { result ->
        //        result.items.forEach { item ->
        //            Log.i("AmplifyS3", "Item: ${item.key}")
        //        }
        //    },
        //    { Log.e("AmplifyS3", "List failure", it) }
        //)
    }


    private class ImageAnalyzer(ctx: Context, private val listener: RecognitionListener) :
        ImageAnalysis.Analyzer {

        // Add class variable TensorFlow Lite Model
        // Initializing the flowerModel by lazy so that it runs in the same thread when the process
        // method is called.
        private val flowerModel: FlowerModel by lazy{
            // Optional GPU acceleration
            val compatList = CompatibilityList()

            val options = if(compatList.isDelegateSupportedOnThisDevice) {
                Log.d(TAG, "This device is GPU Compatible ")
                Model.Options.Builder().setDevice(Model.Device.GPU).build()
            } else {
                Log.d(TAG, "This device is GPU Incompatible ")
                Model.Options.Builder().setNumThreads(4).build()
            }

            // Initialize the Flower Model
            FlowerModel.newInstance(ctx, options)
        }

        override fun analyze(imageProxy: ImageProxy) {

            val items = mutableListOf<Recognition>()

            // Convert Image to Bitmap then to TensorImage
            val tfImage = TensorImage.fromBitmap(toBitmap(imageProxy))

            // Process the image using the trained model, sort and pick out the top results
            val outputs = flowerModel.process(tfImage)
                .probabilityAsCategoryList.apply {
                    sortByDescending { it.score } // Sort with highest confidence first
                }.take(MAX_RESULT_DISPLAY) // take the top results

            flower_label = outputs[0].label

            //TODO current

            // Converting the top probability items into a list of recognitions
            for (output in outputs) {
                items.add(Recognition(output.label, output.score))
            }

//            // START - Placeholder code at the start of the codelab. Comment this block of code out.
//            for (i in 0 until MAX_RESULT_DISPLAY){
//                items.add(Recognition("Fake label $i", Random.nextFloat()))
//            }
//            // END - Placeholder code at the start of the codelab. Comment this block of code out.

            // Return the result
            listener(items.toList())

            // Close the image,this tells CameraX to feed the next image to the analyzer
            imageProxy.close()
        }

        /**
         * Convert Image Proxy to Bitmap
         */
        private val yuvToRgbConverter = YuvToRgbConverter(ctx)
        private lateinit var bitmapBuffer: Bitmap
        private lateinit var rotationMatrix: Matrix

        @SuppressLint("UnsafeExperimentalUsageError")
        private fun toBitmap(imageProxy: ImageProxy): Bitmap? {

            val image = imageProxy.image ?: return null

            // Initialise Buffer
            if (!::bitmapBuffer.isInitialized) {
                // The image rotation and RGB image buffer are initialized only once
                Log.d(TAG, "Initalise toBitmap()")
                rotationMatrix = Matrix()
                rotationMatrix.postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                bitmapBuffer = Bitmap.createBitmap(
                    imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
                )
            }

            // Pass image to an image analyser
            yuvToRgbConverter.yuvToRgb(image, bitmapBuffer)

            // Create the Bitmap in the correct orientation
            return Bitmap.createBitmap(
                bitmapBuffer,
                0,
                0,
                bitmapBuffer.width,
                bitmapBuffer.height,
                rotationMatrix,
                false
            )
        }

    }

    companion object {
        private const val TAG = "Plantie"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}

