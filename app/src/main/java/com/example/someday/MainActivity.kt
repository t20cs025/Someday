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
import android.os.Environment
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import  java.io.IOException
import  java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.BitmapFactory

class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var imageView: ImageView

//    private val destination1 = DestinationLocation(35.680420, 138.571538)
//    private val waypoint = DestinationLocation(35.677837, 138.573188)
    private val destination1 = DestinationLocation(35.67797,138.57280)
    private val waypoint = DestinationLocation(35.67715,138.57514)
    private val Manpk = DestinationLocation(35.676020, 138.572422)
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private var notificationLocation: Location? = null
    private var totalDistanceAfterNotification: Float = 0.0f

    // LocationSensor を lateinit で宣言
    lateinit var locationSensor: LocationSensor

    val CHANNEL_ID = "sample"

    // waypointの付近3mを通過したかどうかを追跡するフラグ
    private var isNearWaypoint: Boolean = false

    //txtファイルへの書き込みをやめるかどうかを判断するフラグ
    private var StopInput:Boolean = false
    // waypoint付近3mを通過した時点でのユーザーの位置情報
    private var waypointPassLocation: Location? = null

    // Flag to track if the notification has been sent
    private var notificationSent: Boolean = false
    //記録中かどうかを確認する
    private var isRecording: Boolean = false

    private lateinit var fileHandler: Handler
    private lateinit var locationUpdateRunnable: Runnable

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.picture)
        imageView.setImageResource(R.drawable.sight)

        //通知を作成するより先に必ず実行
        createNotificationChannel()

        textView = findViewById(R.id.textview)

        locationSensor = LocationSensor(this)
        locationSensor.requestLocationPermission()

        fileHandler = Handler()

        locationSensor.location.observe(this, Observer {
            userLatitude = it.latitude
            userLongitude = it.longitude

            var NorthDistance: Int = destination1.getNorthDistance(userLatitude, userLongitude)
            var EastDistance: Int = destination1.getEastDistance(userLatitude, userLongitude)

            val pointDistance = waypoint.getLinearDistance(userLatitude, userLongitude)
            val distance1 = destination1.getLinearDistance(userLatitude, userLongitude)
            val pointEast = waypoint.getEastDistance(userLatitude, userLongitude)
            val pointNorth = waypoint.getNorthDistance(userLatitude, userLongitude)
            val MEast = Manpk.getEastDistance(userLatitude, userLongitude)
            val MNorth = Manpk.getNorthDistance(userLatitude, userLongitude)
            val MDis = Manpk.getLinearDistance(userLatitude, userLongitude)

            textView.text =
                "目標地点\n北方向 : ${NorthDistance}m\n東方向 : ${EastDistance}m"
     //                   "通知開始ポイントまで\n北に${pointNorth}m\n東に${pointEast}m\n合計${pointDistance}m" +
   //                     "測定開始までの距離: ${pointDistance}"

            // Write location information to a text file
//            writeLocationToFile(it)

            // Check if waypoint is near and notification has not been sent
            if (pointDistance < 10 && !notificationSent) {
                createNotification()
                notificationSent = true
                textView.setBackgroundColor(getColor(R.color.lightBlue))
                isRecording = true
            }

            if (distance1 < 5) {
                textView.text = "\n到着しました\n"
                StopInput = true
                textView.setBackgroundColor(getColor(R.color.darkBlue))
                isRecording = false
            }
        })

        // ボタンを押さなくても位置情報の取得を開始
        locationSensor.start()

        // Schedule location updates every 5 seconds
//        scheduleLocationUpdates()
    }

    private fun createNotificationChannel()
     {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "name"
            val descriptionText = "description"
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
        notificationLocation = Location(locationSensor.location.value!!)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Load the bitmap for the big picture
        val bigPicture = BitmapFactory.decodeResource(resources, R.drawable.sight)

        // 通知オブジェクトの作成
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Title")
            .setContentText("Text")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigPictureStyle().bigPicture(bigPicture))
            .setAutoCancel(true)

        // 通知の設定
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, builder.build())

        // 位置情報を記録
        if (!StopInput) {
            scheduleLocationUpdates()
            textView.text = "記録開始"
        }
    }


    private fun writeLocationToFile(location: Location) {

        try {
            if(StopInput)return

            val timestamp = SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date())
            val fileName = "location.csv"

            BufferedWriter(OutputStreamWriter(openFileOutput(fileName, Context.MODE_APPEND))).use { writer ->
                val timestampFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                val data = "$timestampFormatted,${location.latitude},${location.longitude}\n"
                writer.write(data)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
//
private fun scheduleLocationUpdates() {
    val delay: Long = 5000 // 5 seconds
    locationUpdateRunnable = object : Runnable {
        override fun run() {
            locationSensor.location.value?.let {
                // Write location information to a text file
                writeLocationToFile(it)
            }
            // Reset the notification flag for the next cycle
//            notificationSent = false
            // Schedule the next location update
            fileHandler.postDelayed(this, delay)
        }
    }
    fileHandler.postDelayed(locationUpdateRunnable, delay)
}

    override fun onDestroy() {
        super.onDestroy()
        // アクティビティが破棄される際にハンドラーからコールバックを削除
        fileHandler.removeCallbacks(locationUpdateRunnable)
    }
}
