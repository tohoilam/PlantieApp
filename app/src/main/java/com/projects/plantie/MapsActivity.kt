package com.projects.plantie

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.amplifyframework.storage.options.StorageListOptions
import com.amplifyframework.storage.options.StorageUploadFileOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.tensorflow.lite.examples.plantie.R
import org.tensorflow.lite.examples.plantie.databinding.ActivityMapsBinding
import java.io.File


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var cardListData: List<CardModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Request storage permissions
        if (allPermissionsGranted()) {
        } else {
            ActivityCompat.requestPermissions(
                this, MapsActivity.REQUIRED_PERMISSIONS, MapsActivity.REQUEST_CODE_PERMISSIONS
            )
        }

        Amplify.Auth.fetchAuthSession(
            {
                Log.i("AmplifyCheckLogin", "Auth session = $it")
                if (it.isSignedIn){
                    val options = StorageListOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .build()
                    val cloudfilenameIds = mutableListOf<String>()

                    Amplify.Storage.list("", options,
                        { result ->
                            result.items.forEach { item ->
                                Log.i("AmplifyS3", "Item: ${item.key}")
                                cloudfilenameIds.add(item.key)
                            }
                            syncPhotos(cloudfilenameIds)
                        },
                        { Log.e("AmplifyS3", "List failure", it) }
                    )
                }else{ //cloud not available, use local only
                    runOnUiThread(Runnable {
                        val sharedPreferences = getSharedPreferences("Plantie", MODE_PRIVATE)
                        val filenameIds = sharedPreferences.all.map { it.key }.toMutableList()
                        val filenameValues = sharedPreferences.all.map { it.value }.toMutableList()
                        val filepath = sharedPreferences.getString("filepath", "")
                        filenameIds.remove("filepath")
                        filenameValues.remove(filepath)
                        Log.i("LocalStorage", filenameIds.toString())
                        Log.i("LocalStorage", filepath!!)
                        //checking complete, pass info to page
                        val cardList = arrayListOf<CardModel>()

                        for (i in filenameIds.indices) {
                            val temp = filepath + "/" + filenameIds[i]
                            Log.i("CardList", temp)
                            var data = filenameIds[i].split(";")
                            Log.i("CardList", data.toString())
                            Log.i("CardList", filenameIds[i].toString())
                            Log.i("CardList", "herehere Lat:  " + data[2].toString())
                            Log.i("CardList", "herehere Long: " + data[3].toString())
                            cardList.add(CardModel(R.drawable.lilyvalley, data[1], data[0], temp, data[2].toDouble(), data[3].replace(".jpg", "").toDouble()
                            ))
                        }

                        Log.d("browse_final", "Adapter Browse")

                        cardListData = cardList
                        drawCard()
                    })
                }
            },
            { error -> Log.e("AmplifyCheckLogin", "Failed to fetch auth session", error) }
        )
    }

    private fun drawCard() {
        mMap.clear()

        for (card in cardListData) {
            val markerView = (getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.map_card, null)
            val image = markerView.findViewById<ImageView>(R.id.flower_image)
            val text = markerView.findViewById<TextView>(R.id.flower_name)

            text.text = card.name
            var imgFile =  File(card.imagePath)

            if(imgFile.exists()){
                var myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                image.setImageBitmap(myBitmap)
            }

            val location = LatLng(card.lat!!, card.long!!)

            val bitmap = Bitmap.createScaledBitmap(viewToBitmap(markerView)!!, markerView.width, markerView.height, false)
            val icon = BitmapDescriptorFactory.fromBitmap(bitmap)
            mMap.addMarker(MarkerOptions().position(location).icon(icon))
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val hongKong = LatLng(22.3193, 114.1694)
        val zoom = CameraUpdateFactory.zoomTo(10f)

        mMap.moveCamera(CameraUpdateFactory.newLatLng(hongKong))
        mMap.animateCamera(zoom)

        drawCard()
    }

    private fun viewToBitmap(view: View): Bitmap? {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        view.draw(canvas)
        return bitmap
    }

    private fun syncPhotos(cloudfilenameIds: MutableList<String>){
        runOnUiThread(Runnable {
            //check local
            val sharedPreferences = getSharedPreferences("Plantie", MODE_PRIVATE)
            val filenameIds = sharedPreferences.all.map { it.key }.toMutableList()
            val filenameValues = sharedPreferences.all.map { it.value }.toMutableList()
            val filepath = sharedPreferences.getString("filepath",
                Environment.getExternalStorageDirectory().toString()+"/Pictures/Plantie-Image/"
            )
            filenameIds.remove("filepath")
            filenameValues.remove(filepath)
            Log.i("LocalStorage", filenameIds.toString())
            Log.i("LocalStorage", filepath!!)
            val temp = mutableListOf<String>()
            filenameIds.forEach{item ->
                if (item.lastIndexOf(".") > 0) temp.add(item.substring(0, item.lastIndexOf(".")))
                else temp.add(item)
            }
            //compare local and cloud
            Log.i("CloudSync: filenameids", filenameIds.toString())
            Log.i("CloudSync: cloudids", cloudfilenameIds.toString())
            var cloudExtra = cloudfilenameIds.minus(temp)
            Log.i("CloudSync: cloudextra", cloudExtra.toString())
            var localExtra = temp.minus(cloudfilenameIds)
            Log.i("CloudSync: localextra", localExtra.toString())
            if (cloudExtra.isEmpty() && localExtra.isEmpty()){//completely sync
                //nothing to do
                Log.i("CloudSync", "Cloud sync with local, skipping")
                //checking complete, pass info to page
                val cardList = arrayListOf<CardModel>()

                for (i in filenameIds.indices) {
                    val temp = filepath + "/" + filenameIds[i]
                    Log.i("CardList", temp)
                    var data = filenameIds[i].split(";")
                    Log.i("CardList", data.toString())
                    Log.i("CardList", filenameIds[i].toString())
                    cardList.add(CardModel(R.drawable.lilyvalley, data[1], data[0], temp, data[2].toDouble(), data[3].replace(".jpg", "").toDouble()
                    ))
                }

                Log.d("browse_final", "Adapter Browse")

                cardListData = cardList
                drawCard()
            }
            if (!cloudExtra.isEmpty()) {//cloud has more photos
                //download photo from cloud
                Log.i("CloudSync: cloud", "cloud has more photos, sync attempt")
                val options = StorageDownloadFileOptions.builder()
                    .accessLevel(StorageAccessLevel.PRIVATE)
                    .build()
                var i = cloudExtra.size
                cloudExtra.forEach { item ->
                    Log.i(
                        "CloudSync: cloud",
                        "Cacheing ${item} to ${applicationContext.filesDir}, ${filepath}"
                    )
                    val file = File("${applicationContext.filesDir}/${item}.jpg")
                    Amplify.Storage.downloadFile(item, file, options,
                        {
                            Log.i("CloudSync: cloud", "Successfully downloaded: ${it.file.name}")
                            var path = File(filepath + "/${item}.jpg/")
                            Log.i("CloudSync: cloud", "Saving to: ${path}")
                            try {
                                if (path.exists()) {
                                    path.delete()
                                }
                                var copyToLocal = file.copyTo(path!!, true)
                                Log.i(
                                    "CloudSync: cloud",
                                    "Saved to: ${copyToLocal}, ${filepath}/${item}.jpg"
                                )
                                //save info to local
                                val sharedPreferences: SharedPreferences =
                                    getSharedPreferences("Plantie", MODE_PRIVATE)
                                val myEdit: SharedPreferences.Editor = sharedPreferences.edit()
                                myEdit.putString(item, item)
                                myEdit.commit()
                                i -= 1
                                if (i == 0) {
                                    recreate()
                                }
                            } catch (e: Exception) {
                                Log.e("CloudSync: cloud", "Copy cache to gallery failed", e)
                            }
                        },
                        { Log.e("CloudSync: cloud", "Download Failure", it) }
                    )
                }
            }
            if (!localExtra.isEmpty()) {//local has more photos
                //save local to cloud
                Log.i("CloudSync: local", "local has more photos, sync attempt")
                val options = StorageUploadFileOptions.builder()
                    .accessLevel(StorageAccessLevel.PRIVATE)
                    .build()
                localExtra.forEach { time ->
                    Log.i("AmplifyS3", "Trying to sync local to cloud from ${time}")
                    Amplify.Storage.uploadFile(time, File(filepath+"/${time}.jpg"), options,
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
            //checking complete, pass info to page
            val cardList = arrayListOf<CardModel>()

            for (i in filenameIds.indices) {
                val temp = filepath + "/" + filenameIds[i]
                Log.i("CardList", temp)
                var data = filenameIds[i].split(";")
                Log.i("CardList", data.toString())
                Log.i("CardList", filenameIds[i].toString())
                cardList.add(CardModel(R.drawable.lilyvalley, data[1], data[0], temp, data[2].toDouble(), data[3].replace(".jpg", "").toDouble()
                ))
            }

            Log.d("browse_final", "Adapter Browse")

            cardListData = cardList
            drawCard()

        })
    }

    private fun allPermissionsGranted(): Boolean = MapsActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                    add(Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
                }
            }.toTypedArray()
    }
}