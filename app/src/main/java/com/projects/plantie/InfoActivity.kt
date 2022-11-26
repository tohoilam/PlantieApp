package com.projects.plantie

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.tensorflow.lite.examples.plantie.R
import java.io.File
import java.io.IOException

class InfoActivity : AppCompatActivity() {
//    var scientificNameBlock: TextView? = null
    private var urlPrefix: String? = "https://www.rhs.org.uk/plants/"
    private var urlPostfix: String? = "/details"
    private var url: String? = ""
    private var flowerName: String? = "Buttercup"
    private var localImageLoc: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val bundle: Bundle? = intent.extras
        bundle?.let {
            bundle.apply {
                flowerName = getString("flowerName")

                if (containsKey("localImage")) {
                    localImageLoc = getString("localImage")
                }
            }
        }

        url = getUrl()
        if (url != "") {
            AssignInfo(this).execute()
        }
    }

    private fun changeImage() {
        var image: ImageView = findViewById(R.id.flower_image)

        // If local image exist, NOT TESTED
        if (localImageLoc != "") {
            var imgFile =  File(localImageLoc)

            if(imgFile.exists()){
                var myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                image.setImageBitmap(myBitmap)
            }
            return
        }

        when (flowerName) {
            "Buttercup" -> {
                image.setImageResource(R.drawable.buttercup)
            }
            "Colts'Foot" -> {
                image.setImageResource(R.drawable.coltsfoot)
            }
            "Daffodil" -> {
                image.setImageResource(R.drawable.daffodil)
            }
            "Daisy" -> {
                image.setImageResource(R.drawable.daisy)
            }
            "Dandelion" -> {
                image.setImageResource(R.drawable.dandelion)
            }
            "Fritillary" -> {
                image.setImageResource(R.drawable.fritillary)
            }
            "Iris" -> {
                image.setImageResource(R.drawable.iris)
            }
            "Pansy" -> {
                image.setImageResource(R.drawable.pansy)
            }
            "Sunflower" -> {
                image.setImageResource(R.drawable.sunflower)
            }
            "Windflower" -> {
                image.setImageResource(R.drawable.windflower)
            }
            "Snowdrop" -> {
                image.setImageResource(R.drawable.snowdrop)
            }
            "LilyValley" -> {
                image.setImageResource(R.drawable.lilyvalley)
            }
            "Bluebell" -> {
                image.setImageResource(R.drawable.bluebell)
            }
            "Crocus" -> {
                image.setImageResource(R.drawable.crocus)
            }
            "Tigerlily" -> {
                image.setImageResource(R.drawable.tigerlily)
            }
            "Tulip" -> {
                image.setImageResource(R.drawable.tulip)
            }
            "Cowslip" -> {
                image.setImageResource(R.drawable.cowslip)
            }
            else -> {
                //TODO: Toast of error message for invalid flower name

            }
        }
    }

    private fun getUrl(): String {
        when (flowerName) {
            "Buttercup" -> {
                return urlPrefix + "195335/hemerocallis-buttercup-parade" + urlPostfix
            }
            "Colts'Foot" -> {
                return urlPrefix + "18563/tussilago-farfara" + urlPostfix
            }
            "Daffodil" -> {
                return urlPrefix + "24054/narcissus-assoanus-(13)" + urlPostfix
            }
            "Daisy" -> {
                return urlPrefix + "94326/bellis-perennis" + urlPostfix
            }
            "Dandelion" -> {
                return urlPrefix + "232327/taraxacum-officinale" + urlPostfix
            }
            "Fritillary" -> {
                return urlPrefix + "7403/fritillaria-meleagris" + urlPostfix
            }
            "Iris" -> {
                return urlPrefix + "47762/iris-pseudacorus-alba" + urlPostfix
            }
            "Pansy" -> {
                return urlPrefix + "18990/viola-cornuta" + urlPostfix
            }
            "Sunflower" -> {
                return urlPrefix + "105515/helianthus-annuus" + urlPostfix
            }
            "Windflower" -> {
                return urlPrefix + "49988/anemone-vernalis" + urlPostfix
            }
            "Snowdrop" -> {
                return urlPrefix + "7568/galanthus-nivalis" + urlPostfix
            }
            "LilyValley" -> {
                return urlPrefix + "4327/convallaria-majalis" + urlPostfix
            }
            "Bluebell" -> {
                return urlPrefix + "8890/hyacinthoides-non-scripta" + urlPostfix
            }
            "Crocus" -> {
                return urlPrefix + "29659/crocus-vernus" + urlPostfix
            }
            "Tigerlily" -> {
                return urlPrefix + "91726/lilium-lancifolium-splendens-(ixc-d)" + urlPostfix
            }
            "Tulip" -> {
                return urlPrefix + "346666/tulipa-budlight-(6)" + urlPostfix
            }
            "Cowslip" -> {
                return urlPrefix + "13892/primula-veris-(pr)" + urlPostfix
            }
            else -> {
                //TODO: Toast of error message for invalid flower name

                return ""
            }
        }

    }

    inner class AssignInfo(private var activity: InfoActivity?) : AsyncTask<String, String, String>() {
        var scientificName: String? = ""
        var description: String? = ""
        var maxHeight: String? = ""
        var maxSpread: String? = ""
        var timeToMaxHeight: String? = ""
        var conditionA: String? = ""
        var conditionB: String? = ""
        var conditionC: String? = ""
        var conditionD: String? = ""
        var moisture: String? = ""
        var ph: String? = ""

        override fun onPreExecute() {
            super.onPreExecute()
            setContentView(R.layout.activity_info)
        }

        override fun doInBackground(vararg p0: String?): String? {
            var document: Document? = null

            try {
                Log.d("InfoActivity", "Connecting to plant website")
                document = Jsoup.connect(url).get()
                Log.d("InfoActivity", "Successful plant website")
            } catch (e: IOException) {
                e.printStackTrace()

                Log.d("InfoActivity", "Failed")
            }

            if (document != null) {
                // Start scraping
                scientificName = document.selectFirst("h1 span")!!.text()
                description = document.selectFirst(".l-module__content > p.ng-star-inserted")!!.text()
                maxHeight = document.select(".plant-attributes__panel:nth-child(1) .plant-attributes__content div:has(> h6)")[0]!!.ownText()
                maxSpread = document.select(".plant-attributes__panel:nth-child(1) .plant-attributes__content div:has(> h6)")[2]!!.ownText()
                timeToMaxHeight = document.select(".plant-attributes__panel:nth-child(1) .plant-attributes__content div:has(> h6)")[1]!!.ownText()

                val conditionList = document.select(".plant-attributes__panel:nth-child(2) .plant-attributes__content div.flag__body")
                conditionA = if (conditionList.size >= 1) conditionList[0]!!.text() else ""
                conditionB = if (conditionList.size >= 2) conditionList[1]!!.text() else ""
                conditionC = if (conditionList.size >= 3) conditionList[2]!!.text() else ""
                conditionD = if (conditionList.size >= 4) conditionList[3]!!.text() else ""
                moisture = document.select(".plant-attributes__panel:nth-child(2) .plant-attributes__content div:has(> h6)")[0]!!.text().replace("Moisture", "")
                ph = document.select(".plant-attributes__panel:nth-child(2) .plant-attributes__content div:has(> h6)")[1]!!.text().replace("pH", "")
            }

            return "Successful"
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            // Find position in xml
            val plantNameBlock: TextView = findViewById(R.id.plant_name)
            val scientificNameBlock: TextView = findViewById(R.id.scientific_name)
            val descriptionBlock: TextView = findViewById(R.id.description)
            val maxHeightBlock: TextView = findViewById(R.id.max_height)
            val maxSpreadBlock: TextView = findViewById(R.id.max_spread)
            val timeToMaxHeightBlock: TextView = findViewById(R.id.time_to_max_height)
            val conditionABlock: TextView = findViewById(R.id.condition_A)
            val conditionBBlock: TextView = findViewById(R.id.condition_B)
            val conditionCBlock: TextView = findViewById(R.id.condition_C)
            val conditionDBlock: TextView = findViewById(R.id.condition_D)
            val moistureBlock: TextView = findViewById(R.id.moisture)
            val phBlock: TextView = findViewById(R.id.ph)

            changeImage()

            // Write texts
            plantNameBlock!!.text = flowerName
            scientificNameBlock!!.text = scientificName
            descriptionBlock!!.text = description
            maxHeightBlock!!.text = maxHeight
            maxSpreadBlock!!.text = maxSpread
            timeToMaxHeightBlock!!.text = timeToMaxHeight
            conditionABlock!!.text = conditionA
            conditionBBlock!!.text = conditionB
            conditionCBlock!!.text = conditionC
            conditionDBlock!!.text = conditionD
            moistureBlock!!.text = moisture
            phBlock!!.text = ph
        }


    }
}