package com.example.someday

import android.location.Location
import kotlin.math.roundToInt

class DestinationLocation(latitude: Double, longitude: Double) {

    private val destinationLatitude: Double = latitude
    private val destinationLongitude: Double = longitude

    public fun getNorthDistance(userLatitude: Double, userLongitude: Double): Int {
        return calculateNorthDistanceToDestination(userLatitude, userLongitude).roundToInt()
    }

    public fun getEastDistance(userLatitude: Double, userLongitude: Double): Int {
        return calculateEastDistanceToDestination(userLatitude, userLongitude).roundToInt()
    }

    public fun getLinearDistance(userLatitude:Double, userLongitude: Double): Float {
        return calculateDistance(userLatitude,userLongitude)
    }
    public fun calculateDistance(userLatitude: Double, userLongitude: Double): Float {
        val results: FloatArray = FloatArray(3)
        // 距離を計算
        Location.distanceBetween(
            destinationLatitude,
            destinationLongitude,
            userLatitude,
            userLongitude,
            results
        )
        return results[0]
    }
//
//    private fun calculateDistanceToDestination(userLatitude: Double, userLongitude: Double): Float {
//
//        val results: FloatArray = FloatArray(3)
//
//        // 距離を計算
//        Location.distanceBetween(
//            destinationLatitude,
//            destinationLongitude,
//            userLatitude,
//            userLongitude,
//            results
//        )
//        return results[0]
//    }

    private fun calculateNorthDistanceToDestination(
        userLatitude: Double,
        userLongitude: Double
    ): Double {
        val destinationAzimuth = calculateAzimuthToDestination(userLatitude, userLongitude)

        // 北極と目的地の方位を計算
        val polarAzimuth = 0.0

        // 北方向への角度を計算（絶対値）
        val northDistance = kotlin.math.abs(destinationAzimuth - polarAzimuth)

        // 180度より大きい場合は補正する
        return if (northDistance > 180.0) 360.0 - northDistance else northDistance
    }

    private fun calculateEastDistanceToDestination(
        userLatitude: Double,
        userLongitude: Double
    ): Double {
        val destinationAzimuth = calculateAzimuthToDestination(userLatitude, userLongitude)

        // 東方向と目的地の方位を計算
        val eastAzimuth = 90.0

        // 東方向への角度を計算（絶対値）
        val eastDistance = kotlin.math.abs(destinationAzimuth - eastAzimuth)

        // 180度より大きい場合は補正する
        return if (eastDistance > 180.0) 360.0 - eastDistance else eastDistance
    }

    private fun calculateAzimuthToDestination(userLatitude: Double, userLongitude: Double): Double {
        // 緯度経度から方位を計算
        val azimuth: Double = kotlin.math.atan2(
            kotlin.math.sin(Math.toRadians(destinationLongitude - userLongitude)),
            kotlin.math.cos(Math.toRadians(userLatitude)) * kotlin.math.tan(
                Math.toRadians(
                    destinationLatitude
                )
            ) -
                    kotlin.math.sin(Math.toRadians(userLatitude)) * kotlin.math.cos(
                Math.toRadians(
                    destinationLongitude - userLongitude
                )
            )
        )

        // ラジアンを度に変換して方位を返す
        return Math.toDegrees(azimuth)
    }

    fun getDestinationLatitude(): Double {
        return destinationLatitude
    }

    fun getDestinationLongitude(): Double {
        return destinationLongitude
    }
}
