package com.example.someday

import android.location.Location
import kotlin.math.roundToInt

class DestinationLocation(latitude: Double, longitude: Double) {

    private val destinationLatitude: Double = latitude
    private val destinationLongitude: Double = longitude

    public fun getNorthDistance(userLatitude: Double, userLongitude: Double): Int {
        return calculateNorthDistance(userLatitude, userLongitude).roundToInt()
    }

    public fun getEastDistance(userLatitude: Double, userLongitude: Double): Int {
        return calculateEastDistance(userLatitude, userLongitude).roundToInt()
    }

    public fun getLinearDistance(userLatitude:Double, userLongitude: Double): Int {
        return calculateDistance(userLatitude,userLongitude).roundToInt()
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



    private fun calculateEastDistance(
        userLatitude: Double,
        userLongitude: Double
    ): Float{
        val results: FloatArray = FloatArray(3)
        // 距離を計算

        Location.distanceBetween(
            0.0,
            destinationLongitude,
            0.0,
            userLongitude,
            results
        )
        return if (results[1] in 0.0..180.0)
            -results[0]
        else results[0]
    }
    private fun calculateNorthDistance(
        userLatitude: Double,
        userLongitude: Double
    ): Float{
        val results: FloatArray = FloatArray(3)
        // 距離を計算
        Location.distanceBetween(

            destinationLatitude,
            0.0,
            userLatitude,
            0.0,
            results
        )
        return if (results[1] in 90.0..270.0)
            results[0]
        else -results[0]
    }

    fun getDestinationLatitude(): Double {
        return destinationLatitude
    }

    fun getDestinationLongitude(): Double {
        return destinationLongitude
    }
}
