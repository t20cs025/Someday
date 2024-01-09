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
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import kotlin.math.roundToInt
class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView
    lateinit var imageView: ImageView

    private val destination1 = DestinationLocation(35.680420, 138.571538)
    private val waypoint = DestinationLocation(35.677837, 138.573188)
    private val Manpk = DestinationLocation(35.676020,138.572422)
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0

    // LocationSensor を lateinit で宣言
    lateinit var locationSensor: LocationSensor

    val CHANNEN_ID = "sample"

    // waypointの付近3mを通過したかどうかを追跡するフラグ
    private var isNearWaypoint: Boolean = false

    // waypoint付近3mを通過した時点でのユーザーの位置情報
    private var waypointPassLocation: Location? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.picture)
        imageView.setImageResource(R.drawable.sight)

        //通知を作成するより先に必ず実行
        createNotificationChannel()

        textView = findViewById(R.id.textview)
//        imageView = findViewById(R.id.imageView3)
//        imageView.setImageResource(R.drawable.img_4768)
        val distance1 = destination1.calculateDistance(userLatitude, userLongitude)



        locationSensor = LocationSensor(this)
        locationSensor.requestLocationPermission()


        locationSensor.location.observe(this, Observer {
            userLatitude = it.latitude
            userLongitude = it.longitude
            // waypoint付近3mを通過した後の移動距離
            var distanceAfterWaypoint: Int = 0

            if (!isNearWaypoint) {
                // waypointの付近3mを通過したかどうかをチェック
                if (waypoint.getLinearDistance(userLatitude, userLongitude) < 3) {
                    isNearWaypoint = true
                    waypointPassLocation = Location(it) // ユーザーがwaypointの付近3mを通過した位置を保存
                }
            } else {
                // waypointの付近3mを通過した後の移動距離を計算
                distanceAfterWaypoint = it.distanceTo(waypointPassLocation!!).roundToInt()
            }

            var NorthDistance: Int = destination1.getNorthDistance(userLatitude, userLongitude)
            var EastDistance: Int = destination1.getEastDistance(userLatitude, userLongitude)


            val pointDistance = waypoint.getLinearDistance(userLatitude, userLongitude)
            val pointEast = waypoint.getEastDistance(userLatitude, userLongitude)
            val pointNorth = waypoint.getNorthDistance(userLatitude, userLongitude)
            val MEast = Manpk.getEastDistance(userLatitude, userLongitude)
            val MNorth = Manpk.getNorthDistance(userLatitude,userLongitude)
            val MDis =  Manpk.getLinearDistance(userLatitude, userLongitude)
            textView.text =
                "目標地点\n北方向 : ${NorthDistance}m\n東方向 : ${EastDistance}m\n" +
                        "通知開始ポイントまで\n北に${pointNorth}m\n東に${pointEast}m\n合計${pointDistance}m" +
                        "\n飯屋まで\n北に${MNorth}m, 東に${MEast}m, 直線距離${MDis}m\n" +
                        "測定開始までの距離: ${pointDistance}, 通過後の移動距離: $distanceAfterWaypoint m"
//            textView.text = "北方向 : ${userLatitude}m, 東方向 : ${userLongitude}m"
//            textView.text = "${destination1.calculateDistance(userLatitude,userLongitude)}"
            if ( MDis< 3 || isNearWaypoint) {
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


