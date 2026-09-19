package com.unsmah.pathpulse.location

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure location utility functions with zero Android dependencies.
 *
 * Companion to [TrackMath] — this file works with raw primitives
 * (lat/lon doubles, speed m/s) so it is fully testable on the JVM
 * without any Android framework or Room entities.
 *
 * Shared constants and formulas:
 * - Earth radius: 6 371 000 m (WGS-84 mean)
 * - 1 m/s = 3.6 km/h = 2.236 84 mph
 */
object LocationUtils {

    // ── Earth constants ──────────────────────────────────────────────────────

    /** Earth's mean radius in metres (WGS-84). */
    const val EARTH_RADIUS_M: Double = 6_371_000.0

    /** Metres per kilometre. */
    const val METRES_PER_KM: Double = 1_000.0

    /** Miles per kilometre. */
    const val MILES_PER_KM: Double = 0.621_371

    /** Feet per metre. */
    const val FEET_PER_M: Double = 3.280_84

    /** Converting m/s → km/h. */
    const val MPS_TO_KMH: Double = 3.6

    /** Converting m/s → mph. */
    const val MPS_TO_MPH: Double = 2.236_84

    // ── Haversine distance ────────────────────────────────────────────────────

    /**
     * Haversine distance between two WGS-84 coordinates.
     *
     * @param lat1 latitude of point A (degrees)
     * @param lon1 longitude of point A (degrees)
     * @param lat2 latitude of point B (degrees)
     * @param lon2 longitude of point B (degrees)
     * @return great-circle distance in metres
     */
    fun haversine(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        if (lat1 == lat2 && lon1 == lon2) return 0.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).let { sinIt ->
            sinIt * sinIt +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        }
        val c = 2 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_M * c
    }

    /** Haversine distance returning kilometres. */
    fun haversineKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double = haversine(lat1, lon1, lat2, lon2) / METRES_PER_KM

    /** Haversine distance returning miles. */
    fun haversineMiles(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double = haversineKm(lat1, lon1, lat2, lon2) * MILES_PER_KM

    // ── Speed conversion ─────────────────────────────────────────────────────

    /** Metres per second → kilometres per hour. */
    fun mpsToKmh(speedMps: Double): Double = speedMps * MPS_TO_KMH

    /** Metres per second → miles per hour. */
    fun mpsToMph(speedMps: Double): Double = speedMps * MPS_TO_MPH

    /**
     * Format a speed in m/s as a human-readable string with unit.
     * Uses km/h by default; pass [useMph] to switch to mph.
     *
     * Negative speeds are clamped to zero.
     */
    fun formatSpeed(speedMps: Double, useMph: Boolean = false): String {
        if (speedMps < 0) return "0.0 ${if (useMph) "mph" else "km/h"}"
        val value = if (useMph) mpsToMph(speedMps) else mpsToKmh(speedMps)
        val unit = if (useMph) "mph" else "km/h"
        return "${value.format(dp = 1)} $unit"
    }

    // ── Bearing ───────────────────────────────────────────────────────────────

    /**
     * Initial bearing (forward azimuth) from point A to point B, in degrees
     * clockwise from true north (0 = N, 90 = E, …).
     *
     * @return value normalised to [0, 360)
     */
    fun bearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        if (lat1 == lat2 && lon1 == lon2) return 0.0
        val dLon = Math.toRadians(lon2 - lon1)
        val y = sin(dLon) * cos(Math.toRadians(lat2))
        val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
                sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
        var theta = Math.toDegrees(atan2(y, x))
        if (theta < 0) theta += 360.0
        return theta
    }

    /** Normalise any angle in degrees to [0, 360). */
    fun normalizeBearing(degrees: Double): Double {
        var result = degrees % 360.0
        if (result < 0) result += 360.0
        return result
    }

    /**
     * Format a numeric bearing into an 8-point compass label.
     * N, NE, E, SE, S, SW, W, NW.
     */
    fun formatBearing(direction: Double): String {
        val normalized = normalizeBearing(direction)
        return when {
            normalized < 22.5 || normalized >= 337.5 -> "N"
            normalized < 67.5  -> "NE"
            normalized < 112.5 -> "E"
            normalized < 157.5 -> "SE"
            normalized < 202.5 -> "S"
            normalized < 247.5 -> "SW"
            normalized < 292.5 -> "W"
            else               -> "NW"
        }
    }

    // ── Coordinate formatting ─────────────────────────────────────────────────

    /**
     * Format a latitude or longitude to [dp] decimal places with cardinal suffix.
     *
     * Examples:
     *   formatCoordinate(48.8566, 4, isLatitude = true)  →  "48.8566° N"
     *   formatCoordinate(-0.1278, 4, isLatitude = false) →  "0.1278° W"
     */
    fun formatCoordinate(coord: Double, dp: Int = 4, isLatitude: Boolean): String {
        val absVal = abs(coord)
        val suffix = when {
            isLatitude -> if (coord >= 0) "N" else "S"
            coord >= 0 -> "E"
            else       -> "W"
        }
        return "%.${dp}f° $suffix".format(absVal)
    }

    /**
     * Format a coordinate pair as a compact string (no cardinal suffixes).
     * Uses 4 decimal places (~11 m precision) by default.
     *
     * Example: "48.8566, 2.3522"
     */
    fun formatCoordinatePair(lat: Double, lon: Double, dp: Int = 4): String {
        return "${lat.format(dp)}, ${lon.format(dp)}"
    }

    // ── Elevation formatting ─────────────────────────────────────────────────

    /**
     * Format elevation in metres as "X m (Y ft)".
     * Returns "—" for null or negative values.
     */
    fun formatElevation(metres: Double?): String {
        if (metres == null || metres < 0) return "—"
        val ft = metres * FEET_PER_M
        return "${metres.format(1)} m (${ft.format(1)} ft)"
    }

    /**
     * Format elevation showing only the requested unit.
     * Returns "—" for null or negative values.
     */
    fun formatElevation(metres: Double?, unit: ElevationUnit): String {
        if (metres == null || metres < 0) return "—"
        return when (unit) {
            ElevationUnit.METRES -> "${metres.format(1)} m"
            ElevationUnit.FEET   -> "${(metres * FEET_PER_M).format(1)} ft"
        }
    }

    /** Supported elevation display units. */
    enum class ElevationUnit {
        /** Metric — metres above sea level. */
        METRES,
        /** Imperial — feet above sea level. */
        FEET
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private fun Double.format(dp: Int): String =
        String.format("%.${dp}f", this)
}
