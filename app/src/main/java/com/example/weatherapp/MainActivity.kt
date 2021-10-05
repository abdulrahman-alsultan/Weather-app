 package com.example.weatherapp

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

 class MainActivity : AppCompatActivity() {

    private lateinit var city: TextView
    lateinit var date: TextView
    lateinit var desc: TextView
    lateinit var degree: TextView
    lateinit var low: TextView
    lateinit var high: TextView
    lateinit var sunrise: TextView
    lateinit var sunset: TextView
    lateinit var wind: TextView
    lateinit var pressure: TextView
    lateinit var humidity: TextView

    lateinit var refreshLL: LinearLayout

    private val cityAPIEndpoint = "https://api.openweathermap.org/data/2.5/weather?q="
    var selectedCity = ""
    var country = ""
    private val joinKey = "&appid=a289ac6dfb995ed665c3559082f2c52b"
    private val units = "&units=metric"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        city = findViewById(R.id.city)
        date = findViewById(R.id.date)
        desc = findViewById(R.id.desc)
        degree = findViewById(R.id.degree)
        low = findViewById(R.id.low)
        high = findViewById(R.id.high)
        sunrise = findViewById(R.id.sunriseTime)
        sunset = findViewById(R.id.sunsetTime)
        wind = findViewById(R.id.wind)
        pressure = findViewById(R.id.pressure)
        humidity = findViewById(R.id.humidity)

        // ll -> Linear layout
        refreshLL = findViewById(R.id.refreshLL)

        refreshLL.setOnClickListener {
            if(selectedCity.isNotEmpty() && country.isNotEmpty())
                prepare()
        }

        degree.setOnClickListener {
            val d = degree.text
            if(d != ""){
                if(d.contains('F')){
                    val fahrenheit  = d.subSequence(0, d.lastIndex-2).toString().toFloat()
                    degree.text = "${"%.2f".format((fahrenheit - 32) / 1.8 )}°C"
                }
                else{
                    val celsius  = d.subSequence(0, d.lastIndex-2).toString().toFloat()
                    degree.text = "${"%.2f".format(celsius*1.8 + 32)}°F"
                    
                }
            }
        }

        city.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Please enter the Country (Ex: US, SA) then the city")
            val input = EditText(this)
            val input2 = EditText(this)
            val context: Context = this
            val layout = LinearLayout(context)
            layout.orientation = LinearLayout.VERTICAL


            input.hint = "Please enter the country"
            input.inputType = InputType.TYPE_CLASS_TEXT
            input2.hint = "Please enter the city"
            input2.inputType = InputType.TYPE_CLASS_TEXT

            layout.addView(input)
            layout.addView(input2)
            builder.setView(layout)

            builder.setPositiveButton("Search", DialogInterface.OnClickListener { _, _ ->
                if (input.text.isNotEmpty() && input2.text.isNotEmpty()) {
                    country = input.text.toString()
                    selectedCity = input2.text.toString()
                    prepare()
                }
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { _, _ -> })
            builder.show()
        }

    }

     private fun prepare(){
         CoroutineScope(IO).launch {
             val response = async {
                 var tmp = ""
                 val url = "$cityAPIEndpoint$selectedCity,$country$units$joinKey"
                 try {
                     tmp = URL(url).readText(Charsets.UTF_8)
                 } catch (e: Exception) {
                     Log.d("TTTT", "ERROR, $url")
                     Log.d("TTTT", "ERROR, $e")
                 }
                 tmp
             }.await()
             if (response.isNotEmpty()) {
                 Log.d("SSSS", response)
                 fetchData(response)
             }
         }
     }


     @SuppressLint("SetTextI18n", "SimpleDateFormat")
     private suspend fun fetchData(response: String){
         withContext(Main){
             try {
                 val jsonObject = JSONObject(response)
                 val main = jsonObject.getJSONObject("main")
                 val sys = jsonObject.getJSONObject("sys")
                 val windJ = jsonObject.getJSONObject("wind")
                 val weather = jsonObject.getJSONArray("weather").getJSONObject(0)

                 city.text = "$selectedCity, $country"
                 date.text = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(jsonObject.getLong("dt")*1000))
                 desc.text = weather.getString("description")
                 degree.text = "${main.getString("temp")}°C"
                 low.text = "Low: ${main.getString("temp_min")}°C"
                 high.text = "High: ${main.getString("temp_max")}°C"
                 sunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sys.getLong("sunrise")*1000))
                 sunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sys.getLong("sunset")*1000))
                 wind.text = windJ.getString("speed")
                 pressure.text = main.getString("pressure")
                 humidity.text = main.getString("humidity")
             }catch (e: Exception){
                 city.text = "Error occur, please try again"
                 desc.text = ""
                 degree.text = ""
                 low.text = ""
                 high.text = ""
                 sunrise.text =""
                 sunset.text = ""
                 wind.text = ""
                 pressure.text = ""
                 humidity.text = ""
             }
         }
     }
}