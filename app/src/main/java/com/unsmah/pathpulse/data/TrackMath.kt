package com.unsmah.pathpulse.data

import com.unsmah.pathpulse.data.entity.TrackPoint

/**
 * Pure mathematical functions for track calculations.
 * Extracted from TrackRepository to be unit-testable without Android dependencies.
 */
object TrackMath {

    /**
     * Earth's radius in meters.
     */
    private const val EARTH_RADIUS_M = 6371000.0

    /**
     * Calculate the haversine distance between two geographic points.
     *
     * @param lat1 Latitude of point 1 (degrees)
     * @param lon1 Longitude of point 1 (degrees)
     * @param lat2 Latitude of point 2 (degrees)
     * @param lon2 Longitude of point 2 (degrees)
     * @return Distance in meters
     */
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return EARTH_RADIUS_M * c
    }

    /**
     * Calculate total distance for a list of track points.
     *
     * @param points List of track points in order
     * @return Total distance in meters
     */
    fun calculateDistance(points: List<TrackPoint>): Double {
        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 1 until points.size) {
            total += haversine(
                points[i - 1].latitude, points[i - 1].longitude,
                points[i].latitude, points[i].longitude
            )
        }
        return total
    }

    /**
     * Estimate moving time from track points.
     * Time is counted as "moving" when speed between consecutive points exceeds the threshold.
     *
     * @param points List of track points in order
     * @param speedThresholdMps Minimum speed (m/s) to count as moving. Default 0.5 m/s (~1.8 km/h)
     * @return Moving time in milliseconds
     */
    fun estimateMovingTime(points: List<TrackPoint>, speedThresholdMps: Double = 0.5): Long {
        if (points.size < 2) return 0L
        var moving = 0L
        for (i in 1 until points.size) {
            val dt = points[i].timestamp - points[i - 1].timestamp
            val dist = haversine(
                points[i - 1].latitude, points[i - 1].longitude,
                points[i].latitude, points[i].longitude
            )
            val speed = if (dt > 0) dist / (dt / 1000.0) else 0.0
            if (speed > speedThresholdMps) {
                moving += dt
            }
        }
        return moving
    }

    /**
     * Calculate elevation gain and loss from track points.
     *
     * @param points List of track points in order
     * @return Pair of (elevationGain, elevationLoss) in meters
     */
    fun calculateElevationChange(points: List<TrackPoint>): Pair<Double, Double> {
        var gain = 0.0
        var loss = 0.0
        for (i in 1 until points.size) {
            val prev = points[i - 1].altitude
            val curr = points[i].altitude
            if (prev != null && curr != null) {
                val diff = curr - prev
                if (diff > 0) gain += diff
                else loss += -diff
            }
        }
        return Pair(gain, loss)
    }

    /**
     * Calculate average speed from total distance and duration.
     *
     * @param distanceMeters Total distance in meters
     * @param durationMillis Total duration in milliseconds
     * @return Average speed in meters per second
     */
    fun calculateAvgSpeed(distanceMeters: Double, durationMillis: Long): Double {
        return if (durationMillis > 0) distanceMeters / (durationMillis / 1000.0) else 0.0
    }

    /**
     * Find the maximum speed from a list of track points.
     *
     * @param points List of track points
     * @return Maximum speed in meters per second, or 0.0 if no speeds available
     */
    fun findMaxSpeed(points: List<TrackPoint>): Double {
        return points.maxOfOrNull { it.speed ?: 0.0 } ?: 0.0
    }
}
