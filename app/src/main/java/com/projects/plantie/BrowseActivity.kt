package com.projects.plantie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.amplifyframework.storage.options.StorageListOptions
import com.amplifyframework.storage.options.StorageUploadFileOptions
import kotlinx.coroutines.delay
import org.tensorflow.lite.examples.plantie.R
import java.io.File

class BrowseActivity : AppCompatActivity() {
    private var gridLayout: GridLayout? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browse)
        gridLayout = findViewById(R.id.browse_box)

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
                            Log.i("CardList", filenameValues[i].toString())
                            cardList.add(
                                CardModel(
                                    R.drawable.lilyvalley,
                                    filenameValues[i].toString(),
                                    filenameIds[i].toString(),
                                    temp
                                )
                            )
                        }

                        Log.d("browse_final", "Adapter Browse")

                        recyclerView = findViewById(R.id.recycler_view)
                        recyclerView.layoutManager = GridLayoutManager(this, 2)
                        recyclerView.adapter = BrowseAdapter(cardList)
                    })
                }
            },
            { error -> Log.e("AmplifyCheckLogin", "Failed to fetch auth session", error) }
        )
    }

    private fun addCard(plantName: String, dateTime: String) {
        val view: View = layoutInflater.inflate(R.layout.card, null)

        val name: TextView = view.findViewById(R.id.flower_name)
        val date: TextView = view.findViewById(R.id.capture_time)

        view!!.setOnClickListener{ toInfoPage(plantName) }

        name.text = plantName
        date.text = dateTime

        gridLayout!!.addView(view)
    }

    private fun syncPhotos(cloudfilenameIds: MutableList<String>){
        runOnUiThread(Runnable {
            //check local
            val sharedPreferences = getSharedPreferences("Plantie", MODE_PRIVATE)
            val filenameIds = sharedPreferences.all.map { it.key }.toMutableList()
            val filenameValues = sharedPreferences.all.map { it.value }.toMutableList()
            val filepath = sharedPreferences.getString("filepath", "")
            filenameIds.remove("filepath")
            filenameValues.remove(filepath)
            Log.i("LocalStorage", filenameIds.toString())
            Log.i("LocalStorage", filepath!!)
            //compare local and cloud
            var cloudExtra = cloudfilenameIds.minus(filenameIds)
            Log.i("CloudSync: cloudextra", cloudExtra.toString())
            var localExtra = filenameIds.minus(cloudfilenameIds)
            Log.i("CloudSync: localextra", localExtra.toString())
            if (cloudExtra.isEmpty() && localExtra.isEmpty()){//completely sync
                //nothing to do
                Log.i("CloudSync", "Cloud sync with local, skipping")
            }
            if (!cloudExtra.isEmpty()){//cloud has more photos
                //download photo from cloud
                Log.i("CloudSync: cloud", "cloud has more photos, sync attempt")
                val options = StorageDownloadFileOptions.builder()
                    .accessLevel(StorageAccessLevel.PRIVATE)
                    .build()
                cloudExtra.forEach { item ->
                    Log.i("CloudSync: cloud", "Cacheing ${item} to ${applicationContext.filesDir}, ${filepath}")
                    val file = File("${applicationContext.filesDir}/${item}.jpg")
                    Amplify.Storage.downloadFile(item, file, options,
                        {
                            Log.i("CloudSync: cloud", "Successfully downloaded: ${it.file.name}")
                            var path = File(filepath+"/${item}.jpg/")
                            Log.i("CloudSync: cloud", "Saving to: ${path}")
                            var copyToLocal = file.copyTo(path!!,true)
                            Log.i("CloudSync: cloud", "Saved to: ${copyToLocal}, ${filepath}/${item}.jpg")
                        },
                        { Log.e("CloudSync: cloud", "Download Failure", it) }
                    )
                }
                if (!localExtra.isEmpty()) {//local has more photos
                    //save local to cloud
                    Log.i("CloudSync: local", "local has more photos, sync attempt")
                    val options = StorageUploadFileOptions.builder()
                        .accessLevel(StorageAccessLevel.PRIVATE)
                        .build()
                    localExtra.forEach { time ->
                        Amplify.Storage.uploadFile(time, File(filepath), options,
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
                    val temp = filepath + "/" +filenameIds[i]
                    Log.i("CardList", temp)
                    Log.i("CardList", filenameValues[i].toString())
                    cardList.add(CardModel(R.drawable.lilyvalley, filenameValues[i].toString(), filenameIds[i].toString(), temp))
                }

                Log.d("browse_final", "Adapter Browse")

                recyclerView = findViewById(R.id.recycler_view)
                recyclerView.layoutManager = GridLayoutManager(this, 2)
                recyclerView.adapter = BrowseAdapter(cardList)
            }

        })
    }

    private fun toInfoPage(plantName: String) {
        intent = Intent(this, InfoActivity::class.java)
        intent.putExtra("flowerName", plantName);
        startActivity(intent)
    }
}