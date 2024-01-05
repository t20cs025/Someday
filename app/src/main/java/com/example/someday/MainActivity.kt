package com.example.someday

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var startButton: Button
    lateinit var stopButton: Button

    val destination1 = DestinationLocation(35.6769, 138.5762)

    var userLatitude:Double = 0.0
    var userLongitude:Double = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textview)
        startButton = findViewById(R.id.button_start)
        stopButton = findViewById(R.id.button_stop)

        val locationSensor = LocationSensor(this)
        locationSensor.requestLocationPermission()

        stopButton.isEnabled = true

        locationSensor.location.observe(this, Observer {
            userLatitude = it.latitude
            userLongitude = it.longitude
            textView.text = "la = ${it.latitude}\n long = ${it.longitude}"
//            textView.text = "Distance = ${destination1.calculateDistanceToDestination(userLatitude, userLongitude)}"

        })

        startButton.setOnClickListener {
            if (!locationSensor.run) {
                locationSensor.start()
                startButton.isEnabled = false
                stopButton.isEnabled = true
            }
        }

        stopButton.setOnClickListener {
            if (locationSensor.run) {
                locationSensor.stop()
                startButton.isEnabled = true
                stopButton.isEnabled = false
            }
        }

    }

}

