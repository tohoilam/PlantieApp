package com.projects.plantie

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageListOptions
import org.tensorflow.lite.examples.plantie.R

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
                runOnUiThread(Runnable {
                    //check local
                    val sharedPreferences = getSharedPreferences("Plantie", MODE_PRIVATE)
                    val filenameIds = sharedPreferences.all.map { it.key }
                    val filenameValues = sharedPreferences.all.map { it.value }
                    Log.i("LocalStorage", filenameIds.toString())
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
                            },
                            { Log.e("AmplifyS3", "List failure", it) }
                        )
                        //compare local and cloud
                        var cloudExtra = cloudfilenameIds.minus(filenameIds)
                        Log.i("CloudSync", cloudExtra.toString())
                        var localExtra = filenameIds.minus(cloudfilenameIds)
                        Log.i("CloudSync", localExtra.toString())
                        //TODO: syncing between local and cloud
                        if (cloudExtra.isEmpty() && localExtra.isEmpty()){//completely sync

                        } else if (cloudExtra.isEmpty()){//local has more photos

                        } else if (localExtra.isEmpty()) {//cloud has more photos

                        }
                    }else{ //cloud not available, use local only

                    }

                    //checking complete, pass info to page
                    val cardList = arrayListOf<CardModel>()
                    for (i in filenameIds.indices) {
                        cardList.add(CardModel(R.drawable.lilyvalley, filenameValues[i].toString(), filenameIds[i].toString(), filenameIds[i]))
                    }

                    Log.d("browse_final", "Adapter Browse")

                    recyclerView = findViewById(R.id.recycler_view)
                    recyclerView.layoutManager = GridLayoutManager(this, 2)
                    recyclerView.adapter = BrowseAdapter(cardList)
                })
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

    private fun toInfoPage(plantName: String) {
        intent = Intent(this, InfoActivity::class.java)
        intent.putExtra("flowerName", plantName);
        startActivity(intent)
    }
}