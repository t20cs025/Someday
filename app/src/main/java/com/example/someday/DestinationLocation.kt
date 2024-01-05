package com.example.someday

import android.location.Location

class DestinationLocation(latitude: Double, longitude: Double) {

    private val destinationLatitude: Double = latitude
    private val destinationLongitude: Double = longitude

    fun calculateDistanceToDestination(userLatitude: Double, userLongitude: Double) : Float{
//        val userLocation = Point(userLongitude, userLatitude)
//        val destinationLocation = Point(destinationLongitude, destinationLatitude)

        // 座標をメートルに変換するための投影法を指定
//        val crs = Proj.crs("EPSG:4326")
//        val userLocationProjected = Proj.transform(crs, crs, userLocation)
//        val destinationLocationProjected = Proj.transform(crs, crs, destinationLocation)
        val results: FloatArray = FloatArray(3)

        // 距離を計算
        Location.distanceBetween(destinationLatitude, destinationLongitude, userLatitude, userLongitude, results)
        return results[0]
    }

    fun getDestinationLatitude(): Double {
        return destinationLatitude
    }

    fun getDestinationLongitude(): Double {
        return destinationLongitude
    }
}
