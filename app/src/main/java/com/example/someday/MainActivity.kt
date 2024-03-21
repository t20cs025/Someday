package com.example.someday

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory
class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var imageView: ImageView
    lateinit var fileHandler: Handler
    lateinit var locationUpdateRunnable: Runnable

    private val destination1 = DestinationLocation(35.678388,138.571586)
    private val waypoint = DestinationLocation(35.677827,138.573196)
    private var isArrive:Boolean = false
    private  var Counter = 0;
    private lateinit var locationSensor: LocationSensor
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    val CHANNEL_ID = "sample"
    private var notificationSent: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textview)
        // Initially, hide the ImageView
        imageView = findViewById(R.id.picture)
        imageView.visibility = ImageView.INVISIBLE

        createNotificationChannel()

        locationSensor = LocationSensor(this)
        locationSensor.requestLocationPermission()

        fileHandler = Handler()

        locationSensor.location.observe(this, Observer {
            userLatitude = it.latitude
            userLongitude = it.longitude
            val pointDistance = waypoint.getLinearDistance(it.latitude, it.longitude)
            val distance1 = destination1.getLinearDistance(it.latitude, it.longitude)
            var NorthDistance: Int = destination1.getNorthDistance(userLatitude, userLongitude)
            var EastDistance: Int = destination1.getEastDistance(userLatitude, userLongitude)

            if (!notificationSent && pointDistance < 50) {
                createNotification()
                createFile()
                notificationSent = true

                // Show the ImageView after the notification is sent

            }
            if(!notificationSent){
                imageView.visibility = ImageView.VISIBLE
                textView.text =
                    "目標地点まで\n北方向 : ${NorthDistance}m\n東方向 : ${EastDistance}m"
                //                   "通知開始ポイントまで\n北に${pointNorth}m\n東に${pointEast}m\n合計${pointDistance}m" +
                //                     "測定開始までの距離: ${pointDistance}"
            }

            if (distance1 < 20 || isArrive) {
                isArrive = true
                textView.text = "\n到着しました！\n"
                notificationSent = false
            }
            if(isArrive && Counter == 0)
                createFile()
            Counter = 1
        })

        locationSensor.start()
//        scheduleLocationUpdates()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification"
            val descriptionText = "I'll recommend YORIMICHI"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val bigPicture = BitmapFactory.decodeResource(resources, R.drawable.notif)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("寄り道を提案します!!")
            .setContentText("近くにこんな場所があるのですが行ってみませんか!?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bigPicture))
            .setAutoCancel(true)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())
    }

    private fun scheduleLocationUpdates() {
        val delay: Long = 5000 // 5 seconds
        locationUpdateRunnable = object : Runnable {
            override fun run() {
                locationSensor.location.value?.let {
                    writeLocationToFile(it)
                }
                fileHandler.postDelayed(this, delay)
            }
        }
        fileHandler.postDelayed(locationUpdateRunnable, delay)
    }

    override fun onDestroy() {
        super.onDestroy()
        fileHandler.removeCallbacks(locationUpdateRunnable)
    }

    private fun writeLocationToFile(location: Location) {
        try {
            val timestampFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val timestamp = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date())
            val fileName = "$timestampFormatted.txt"

            openFileOutput(fileName, Context.MODE_APPEND).use { outputStream ->
                val data = "$timestampFormatted,${location.latitude},${location.longitude}\n"
                outputStream.write(data.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun createFile() {
        try {
            val timestampFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val timestamp = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date())
            val fileName = "$timestampFormatted.txt"

        openFileOutput(fileName, Context.MODE_APPEND).use { outputStream ->
            val data = "$timestampFormatted\n"
            outputStream.write(data.toByteArray())
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
}
