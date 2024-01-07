package com.example.someday

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView

    val destination1 = DestinationLocation(35.680420, 138.571538)
    val waypoint = DestinationLocation(35.677837, 138.573188)
    var userLatitude: Double = 0.0
    var userLongitude: Double = 0.0

    // LocationSensor を lateinit で宣言
    lateinit var locationSensor: LocationSensor

    val CHANNEN_ID = "sample"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textview)

//        startButton = findViewById(R.id.button_start)
//        stopButton = findViewById(R.id.button_stop)
//        通知を作成するより先に必ず実行
        createNotificationChannel()


        val distance1 = destination1.calculateDistance(userLatitude, userLongitude)
        val pointDistance = waypoint.getLinearDistance(userLatitude, userLongitude)
//        val buttonNotification: Button = findViewById(R.id.buttonNotification)
//        buttonNotification.setOnClickListener {
//
//        }

        locationSensor = LocationSensor(this)
        locationSensor.requestLocationPermission()

//        stopButton.isEnabled = true

        locationSensor.location.observe(this, Observer {
            userLatitude = it.latitude
            userLongitude = it.longitude
            var NorthDistance: Int = destination1.getNorthDistance(userLatitude, userLongitude)
            var EastDistance: Int = destination1.getEastDistance(userLatitude, userLongitude)
            var NorthPoint: Int = waypoint.getNorthDistance(userLatitude, userLongitude)
            var EastPoint: Int = waypoint.getEastDistance(userLatitude, userLongitude)
            textView.text =
                "目標地点\n北方向 : ${NorthDistance}m\n東方向 : ${EastDistance}m\n通知開始ポイントまで\n北に${NorthPoint}m\n東に${EastPoint}m\n合計${pointDistance}m"
//            textView.text = "北方向 : ${userLatitude}m, 東方向 : ${userLongitude}m"
//            textView.text = "${destination1.calculateDistance(userLatitude,userLongitude)}"
            if (pointDistance < 2) {
                createNotification()
            }
            if (distance1 < 2) {
                textView.text = "到着しました"
            }

        })
        // ボタンを押さなくても位置情報の取得を開始
        locationSensor.start()


    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEN_ID, name, importance).apply {
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

//        通知オブジェクトの作成
        var builder = NotificationCompat.Builder(this, CHANNEN_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Title")
            .setContentText("Text")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

//            通知の設定
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())
    }
}


