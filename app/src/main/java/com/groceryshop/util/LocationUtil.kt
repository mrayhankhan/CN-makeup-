package com.groceryshop.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.math.*

/**
 * LocationUtil - GPS location, distance calculation, and delivery estimation
 * 
 * PERMISSION REQUIREMENTS:
 * - Add to AndroidManifest.xml (already done):
 *   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 * 
 * - Request at runtime before calling getCurrentLocation():
 *   ActivityCompat.requestPermissions(activity, 
 *     arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 
 *     LOCATION_PERMISSION_CODE)
 */
object LocationUtil {
    
    private const val EARTH_RADIUS_KM = 6371.0
    private const val BASE_DELIVERY_TIME_MINUTES = 10
    private const val AVERAGE_SPEED_KMH = 20.0 // 20 km/h average delivery speed
    
    /**
     * Get current location using GPS
     * Falls back to null if location unavailable or permission denied
     * 
     * @param context Application context
     * @return Location object or null if unavailable
     */
    suspend fun getCurrentLocation(context: Context): Location? {
        // Check location permission
        if (!hasLocationPermission(context)) {
            return null
        }
        
        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationToken = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Check if location permission is granted
     */
    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Calculate distance between two coordinates using Haversine formula
     * 
     * @param lat1 Latitude of first location
     * @param lon1 Longitude of first location
     * @param lat2 Latitude of second location
     * @param lon2 Longitude of second location
     * @return Distance in kilometers
     */
    fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        
        return EARTH_RADIUS_KM * c
    }
    
    /**
     * Estimate delivery time based on distance
     * Formula: base time + (distance / speed) * 60
     * 
     * @param distanceKm Distance in kilometers
     * @return Estimated delivery time in minutes
     */
    fun estimateDeliveryMinutes(distanceKm: Double): Int {
        val travelTimeMinutes = (distanceKm / AVERAGE_SPEED_KMH) * 60
        val totalTime = BASE_DELIVERY_TIME_MINUTES + travelTimeMinutes
        return ceil(totalTime).toInt()
    }
    
    /**
     * Format location as string
     */
    fun formatLocation(lat: Double, lon: Double): String {
        return String.format("%.6f, %.6f", lat, lon)
    }
    
    /**
     * Parse location string to lat/lng pair
     * Format: "lat, lng" or "lat,lng"
     * @return Pair<lat, lng> or null if invalid
     */
    fun parseLocation(locationString: String): Pair<Double, Double>? {
        return try {
            val parts = locationString.split(",").map { it.trim() }
            if (parts.size == 2) {
                Pair(parts[0].toDouble(), parts[1].toDouble())
            } else null
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Data class for location with optional name
 */
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
) {
    override fun toString(): String = LocationUtil.formatLocation(latitude, longitude)
}
