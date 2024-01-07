package com.example.someday
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*

class LocationSensor(private val activity: Activity) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(activity)

    private var locationCallback: LocationCallback? = null

    private val _location: MutableLiveData<Location> = MutableLiveData()
    val location: LiveData<Location> = _location
    private val destinationLatitude = 35.67852
    private val destinationLongitude = 138.573540
    private val proximityThresholdMeters = 5.0 // 通知を送る範囲の距離（メートル）


    @SuppressLint("MissingPermission")
    fun start() {
        if (checkLocationPermission()) {
            val locationRequest: LocationRequest.Builder =
                LocationRequest.Builder(1000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        _location.postValue(location)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest.build(),
                locationCallback as LocationCallback,
                Looper.getMainLooper()
            )
        }
//        run = true
    }

    fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }



    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    private fun sendNotification(message: String) {
        // NotificationManagerのインスタンスを取得
        val notificationManager =
            activity.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0以降は通知チャンネルが必要
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_id",
                "channel_name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 通知のビルダーを作成
        val builder = NotificationCompat.Builder(activity, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("通知タイトル")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // 通知を送信
        notificationManager.notify(1, builder.build())
    }

}
