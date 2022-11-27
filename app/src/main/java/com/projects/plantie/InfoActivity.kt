package com.projects.plantie

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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
    private var flowerName: String? = "buttercup"
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
            "buttercup" -> {
                image.setImageResource(R.drawable.buttercup)
            }
            "colts_foot" -> {
                image.setImageResource(R.drawable.coltsfoot)
            }
            "daffodil" -> {
                image.setImageResource(R.drawable.daffodil)
            }
            "daisy" -> {
                image.setImageResource(R.drawable.daisy)
            }
            "dandelion" -> {
                image.setImageResource(R.drawable.dandelion)
            }
            "fritillary" -> {
                image.setImageResource(R.drawable.fritillary)
            }
            "iris" -> {
                image.setImageResource(R.drawable.iris)
            }
            "pansy" -> {
                image.setImageResource(R.drawable.pansy)
            }
            "sunflower" -> {
                image.setImageResource(R.drawable.sunflower)
            }
            "windflower" -> {
                image.setImageResource(R.drawable.windflower)
            }
            "snowdrop" -> {
                image.setImageResource(R.drawable.snowdrop)
            }
            "lily_valley" -> {
                image.setImageResource(R.drawable.lilyvalley)
            }
            "bluebell" -> {
                image.setImageResource(R.drawable.bluebell)
            }
            "crocus" -> {
                image.setImageResource(R.drawable.crocus)
            }
            "tigerlily" -> {
                image.setImageResource(R.drawable.tigerlily)
            }
            "tulip" -> {
                image.setImageResource(R.drawable.tulip)
            }
            "cowslip" -> {
                image.setImageResource(R.drawable.cowslip)
            }
            else -> {
                //TODO: Toast of error message for invalid flower name

            }
        }
    }

    private fun getUrl(): String {
        when (flowerName) {
            "buttercup" -> {
                return urlPrefix + "195335/hemerocallis-buttercup-parade" + urlPostfix
            }
            "colts_foot" -> {
                return urlPrefix + "18563/tussilago-farfara" + urlPostfix
            }
            "daffodil" -> {
                return urlPrefix + "24054/narcissus-assoanus-(13)" + urlPostfix
            }
            "daisy" -> {
                return urlPrefix + "94326/bellis-perennis" + urlPostfix
            }
            "dandelion" -> {
                return urlPrefix + "232327/taraxacum-officinale" + urlPostfix
            }
            "fritillary" -> {
                return urlPrefix + "7403/fritillaria-meleagris" + urlPostfix
            }
            "iris" -> {
                return urlPrefix + "47762/iris-pseudacorus-alba" + urlPostfix
            }
            "pansy" -> {
                return urlPrefix + "18990/viola-cornuta" + urlPostfix
            }
            "sunflower" -> {
                return urlPrefix + "105515/helianthus-annuus" + urlPostfix
            }
            "windflower" -> {
                return urlPrefix + "49988/anemone-vernalis" + urlPostfix
            }
            "snowdrop" -> {
                return urlPrefix + "7568/galanthus-nivalis" + urlPostfix
            }
            "lily_valley" -> {
                return urlPrefix + "4327/convallaria-majalis" + urlPostfix
            }
            "bluebell" -> {
                return urlPrefix + "8890/hyacinthoides-non-scripta" + urlPostfix
            }
            "crocus" -> {
                return urlPrefix + "29659/crocus-minimus-spring-beauty" + urlPostfix
            }
            "tigerlily" -> {
                return urlPrefix + "91726/lilium-lancifolium-splendens-(ixc-d)" + urlPostfix
            }
            "tulip" -> {
                return urlPrefix + "346666/tulipa-budlldog-(7)" + urlPostfix
            }
            "cowslip" -> {
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

            // From Json
//            if (flowerName == "daffodil" || flowerName == "sunflower" || flowerName == "lily_valley") {
            val fileContent = resources.openRawResource(R.raw.flowerinfo)
                .bufferedReader().use { it.readText() }

            val flowerInfos: InfoModel = Gson().fromJson(fileContent, object : TypeToken<InfoModel>() {}.type)

            for (flower in flowerInfos.flower_info) {
                if (flower.name == flowerName) {
                    scientificName = flower.scientific_name
                    description = flower.description
                    maxHeight = flower.max_height
                    maxSpread = flower.max_spread
                    timeToMaxHeight = flower.time_to_max_height
                    conditionA = if (flower.conditions.size >= 1) flower.conditions[0] else ""
                    conditionB = if (flower.conditions.size >= 2) flower.conditions[1] else ""
                    conditionC = if (flower.conditions.size >= 3) flower.conditions[2] else ""
                    conditionD = if (flower.conditions.size >= 4) flower.conditions[3] else ""
                    moisture = flower.moisture
                    ph = flower.ph

                    break
                }
            }
//            }
            // Web Scraping
//            else {
//                var document: Document? = null
//
//                try {
//                    Log.d("InfoActivity", "Connecting to plant website")
//                    document = Jsoup.connect(url).get()
//                    Log.d("InfoActivity", "Successful plant website")
//                } catch (e: IOException) {
//                    e.printStackTrace()
//
//                    Log.d("InfoActivity", "Failed")
//                }
//
//                if (document != null) {
//                    // Start scraping
//                    scientificName = document.selectFirst("h1 span")!!.text()
//                    description =
//                        document.selectFirst(".l-module__content > p.ng-star-inserted")!!.text()
//                    maxHeight =
//                        document.select(".plant-attributes__panel:nth-child(1) .plant-attributes__content div:has(> h6)")[0]!!.ownText()
//                    maxSpread =
//                        document.select(".plant-attributes__panel:nth-child(1) .plant-attributes__content div:has(> h6)")[2]!!.ownText()
//                    timeToMaxHeight =
//                        document.select(".plant-attributes__panel:nth-child(1) .plant-attributes__content div:has(> h6)")[1]!!.ownText()
//
//                    val conditionList =
//                        document.select(".plant-attributes__panel:nth-child(2) .plant-attributes__content div.flag__body")
//                    conditionA = if (conditionList.size >= 1) conditionList[0]!!.text() else ""
//                    conditionB = if (conditionList.size >= 2) conditionList[1]!!.text() else ""
//                    conditionC = if (conditionList.size >= 3) conditionList[2]!!.text() else ""
//                    conditionD = if (conditionList.size >= 4) conditionList[3]!!.text() else ""
//                    moisture =
//                        document.select(".plant-attributes__panel:nth-child(2) .plant-attributes__content div:has(> h6)")[0]!!.text()
//                            .replace("Moisture", "")
//                    ph =
//                        document.select(".plant-attributes__panel:nth-child(2) .plant-attributes__content div:has(> h6)")[1]!!.text()
//                            .replace("pH", "")
//                }
//            }

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
            plantNameBlock!!.text = flowerName!!.replace("_", " ").replaceFirstChar(Char::titlecase)
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