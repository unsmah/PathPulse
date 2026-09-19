package com.unsmah.pathpulse.location

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for LocationUtils — pure functions with zero Android dependencies.
 *
 * These tests run on a standard JVM without any Android framework.
 */
class LocationUtilsTest {

    // ──────────────────────────────────────────────────────────────────────────
    // Haversine
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun haversine_samePoint_returnsZero() {
        assertEquals(0.0, LocationUtils.haversine(48.8566, 2.3522, 48.8566, 2.3522), 0.001)
    }

    @Test
    fun haversine_zeroLatLon_returnsZero() {
        assertEquals(0.0, LocationUtils.haversine(0.0, 0.0, 0.0, 0.0), 0.001)
    }

    @Test
    fun haversine_parisToLondon_approxCorrect() {
        val d = LocationUtils.haversine(48.8566, 2.3522, 51.5074, -0.1278)
        assertTrue("Expected ~343000 m, got ${d}m", d in 342900.0..343100.0)
    }

    @Test
    fun haversine_newYorkToLosAngeles_approxCorrect() {
        val d = LocationUtils.haversine(40.7128, -74.0060, 34.0522, -118.2437)
        assertTrue("Expected ~3940000 m, got ${d}m", d in 3935000.0..3945000.0)
    }

    @Test
    fun haversine_1DegreeLatitude_atEquator() {
        val d = LocationUtils.haversine(0.0, 0.0, 1.0, 0.0)
        assertTrue("Expected ~111320 m, got ${d}m", d in 111200.0..111400.0)
    }

    @Test
    fun haversine_1DegreeLongitude_atEquator() {
        val d = LocationUtils.haversine(0.0, 0.0, 0.0, 1.0)
        assertTrue("Expected ~111320 m, got ${d}m", d in 111200.0..111400.0)
    }

    @Test
    fun haversine_1DegreeLongitude_at60N_halfOfEquator() {
        val d = LocationUtils.haversine(60.0, 0.0, 60.0, 1.0)
        assertTrue("Expected ~55800 m at 60N, got ${d}m", d in 55700.0..55900.0)
    }

    @Test
    fun haversine_antimeridian_shortPath() {
        val d = LocationUtils.haversine(0.0, 179.9, 0.0, -179.9)
        assertTrue("Should be small across antimeridian, got ${d}m", d < 250.0)
    }

    @Test
    fun haversine_antipodal_oppositeSidesOfEarth() {
        val d = LocationUtils.haversine(0.0, 0.0, 0.0, 180.0)
        assertTrue("Expected ~20037000 m, got ${d}m", d in 20030000.0..20040000.0)
    }

    @Test
    fun haversine_symmetric() {
        val d1 = LocationUtils.haversine(48.8566, 2.3522, 51.5074, -0.1278)
        val d2 = LocationUtils.haversine(51.5074, -0.1278, 48.8566, 2.3522)
        assertEquals(d1, d2, 0.001)
    }

    @Test
    fun haversine_negativeCoordinates_sydneyToBuenosAires() {
        val d = LocationUtils.haversine(-33.8688, 151.2093, -34.6037, -58.3816)
        assertTrue("Expected ~11900000 m, got ${d}m", d in 11890000.0..11910000.0)
    }

    @Test
    fun haversine_km_convertsCorrectly() {
        val km = LocationUtils.haversineKm(48.8566, 2.3522, 51.5074, -0.1278)
        assertTrue("Expected ~343 km, got ${km}km", km in 342.9..343.1)
    }

    @Test
    fun haversine_miles_convertsCorrectly() {
        val miles = LocationUtils.haversineMiles(48.8566, 2.3522, 51.5074, -0.1278)
        assertTrue("Expected ~213 miles, got ${miles}mi", miles in 212.0..214.0)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Speed conversion
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun mpsToKmh_zero_returnsZero() {
        assertEquals(0.0, LocationUtils.mpsToKmh(0.0), 0.001)
    }

    @Test
    fun mpsToKmh_10mps_returns36() {
        assertEquals(36.0, LocationUtils.mpsToKmh(10.0), 0.001)
    }

    @Test
    fun mpsToKmh_5mps_returns18() {
        assertEquals(18.0, LocationUtils.mpsToKmh(5.0), 0.001)
    }

    @Test
    fun mpsToMph_zero_returnsZero() {
        assertEquals(0.0, LocationUtils.mpsToMph(0.0), 0.001)
    }

    @Test
    fun mpsToMph_10mps_approx22_37() {
        val mph = LocationUtils.mpsToMph(10.0)
        assertTrue("Expected ~22.37 mph, got ${mph}", mph in 22.36..22.38)
    }

    @Test
    fun mpsToMph_5mps_approx11_18() {
        val mph = LocationUtils.mpsToMph(5.0)
        assertTrue("Expected ~11.18 mph, got ${mph}", mph in 11.17..11.19)
    }

    @Test
    fun formatSpeed_zeroKmH() {
        assertEquals("0.0 km/h", LocationUtils.formatSpeed(0.0))
    }

    @Test
    fun formatSpeed_negativeClampedToZero() {
        assertEquals("0.0 km/h", LocationUtils.formatSpeed(-5.0))
    }

    @Test
    fun formatSpeed_10mpsKmH() {
        assertEquals("36.0 km/h", LocationUtils.formatSpeed(10.0))
    }

    @Test
    fun formatSpeed_10mpsMph() {
        val result = LocationUtils.formatSpeed(10.0, useMph = true)
        assertTrue("Expected ~22.4 mph, got '$result'", result.startsWith("22.4 mph") || result.startsWith("22.3 mph"))
    }

    @Test
    fun formatSpeed_5mpsKmH() {
        assertEquals("18.0 km/h", LocationUtils.formatSpeed(5.0))
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Bearing
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun bearing_samePoint_returnsZero() {
        assertEquals(0.0, LocationUtils.bearing(48.8566, 2.3522, 48.8566, 2.3522), 0.001)
    }

    @Test
    fun bearing_dueNorth() {
        val b = LocationUtils.bearing(48.0, 2.0, 49.0, 2.0)
        assertTrue("Expected ~0 deg (N), got ${b} deg", b in 359.0..1.0)
    }

    @Test
    fun bearing_dueEast() {
        val b = LocationUtils.bearing(48.0, 2.0, 48.0, 3.0)
        assertTrue("Expected ~90 deg (E), got ${b} deg", b in 89.0..91.0)
    }

    @Test
    fun bearing_dueSouth() {
        val b = LocationUtils.bearing(48.0, 2.0, 47.0, 2.0)
        assertTrue("Expected ~180 deg (S), got ${b} deg", b in 179.0..181.0)
    }

    @Test
    fun bearing_dueWest() {
        val b = LocationUtils.bearing(48.0, 2.0, 48.0, 1.0)
        assertTrue("Expected ~270 deg (W), got ${b} deg", b in 269.0..271.0)
    }

    @Test
    fun bearing_southeast() {
        val b = LocationUtils.bearing(50.0, 10.0, 49.0, 11.0)
        assertTrue("Expected ~135 deg (SE), got ${b} deg", b in 130.0..140.0)
    }

    @Test
    fun bearing_northwest() {
        val b = LocationUtils.bearing(50.0, 10.0, 51.0, 9.0)
        assertTrue("Expected ~315 deg (NW), got ${b} deg", b in 310.0..320.0)
    }

    @Test
    fun bearing_normalize_negativeAngle() {
        assertEquals(45.0, LocationUtils.normalizeBearing(-315.0), 0.001)
    }

    @Test
    fun bearing_normalize_angleGreaterThan360() {
        assertEquals(45.0, LocationUtils.normalizeBearing(405.0), 0.001)
    }

    @Test
    fun bearing_normalize_720() {
        assertEquals(0.0, LocationUtils.normalizeBearing(720.0), 0.001)
    }

    @Test
    fun formatBearing_N() {
        assertEquals("N", LocationUtils.formatBearing(0.0))
        assertEquals("N", LocationUtils.formatBearing(359.9))
        assertEquals("N", LocationUtils.formatBearing(337.5))
    }

    @Test
    fun formatBearing_NE() {
        assertEquals("NE", LocationUtils.formatBearing(45.0))
        assertEquals("NE", LocationUtils.formatBearing(67.4))
    }

    @Test
    fun formatBearing_E() {
        assertEquals("E", LocationUtils.formatBearing(90.0))
        assertEquals("E", LocationUtils.formatBearing(112.4))
    }

    @Test
    fun formatBearing_SE() {
        assertEquals("SE", LocationUtils.formatBearing(135.0))
        assertEquals("SE", LocationUtils.formatBearing(157.4))
    }

    @Test
    fun formatBearing_S() {
        assertEquals("S", LocationUtils.formatBearing(180.0))
        assertEquals("S", LocationUtils.formatBearing(202.4))
    }

    @Test
    fun formatBearing_SW() {
        assertEquals("SW", LocationUtils.formatBearing(225.0))
        assertEquals("SW", LocationUtils.formatBearing(247.4))
    }

    @Test
    fun formatBearing_W() {
        assertEquals("W", LocationUtils.formatBearing(270.0))
        assertEquals("W", LocationUtils.formatBearing(292.4))
    }

    @Test
    fun formatBearing_NW() {
        assertEquals("NW", LocationUtils.formatBearing(315.0))
        assertEquals("NW", LocationUtils.formatBearing(337.4))
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Coordinate formatting
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun formatCoordinate_northLatitude() {
        assertEquals("48.8566° N", LocationUtils.formatCoordinate(48.8566, 4, isLatitude = true))
    }

    @Test
    fun formatCoordinate_southLatitude() {
        assertEquals("33.8688° S", LocationUtils.formatCoordinate(-33.8688, 4, isLatitude = true))
    }

    @Test
    fun formatCoordinate_eastLongitude() {
        assertEquals("2.3522° E", LocationUtils.formatCoordinate(2.3522, 4, isLatitude = false))
    }

    @Test
    fun formatCoordinate_westLongitude() {
        assertEquals("0.1278° W", LocationUtils.formatCoordinate(-0.1278, 4, isLatitude = false))
    }

    @Test
    fun formatCoordinate_twoDecimalPlaces() {
        assertEquals("48.86° N", LocationUtils.formatCoordinate(48.8566, 2, isLatitude = true))
    }

    @Test
    fun formatCoordinatePair_default() {
        assertEquals("48.8566, 2.3522", LocationUtils.formatCoordinatePair(48.8566, 2.3522))
    }

    @Test
    fun formatCoordinatePair_negativeValues() {
        assertEquals("-33.8688, -58.3816", LocationUtils.formatCoordinatePair(-33.8688, -58.3816))
    }

    @Test
    fun formatCoordinatePair_2dp() {
        assertEquals("48.86, 2.35", LocationUtils.formatCoordinatePair(48.8566, 2.3522, dp = 2))
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Elevation formatting
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    fun formatElevation_normal() {
        val result = LocationUtils.formatElevation(1234.5)
        assertTrue("Expected '1234.5 m' in result, got '$result'", result.contains("1234.5 m"))
        assertTrue("Expected feet equivalent in result, got '$result'", result.contains("ft"))
    }

    @Test
    fun formatElevation_null_returnsDash() {
        assertEquals("—", LocationUtils.formatElevation(null))
    }

    @Test
    fun formatElevation_negative_returnsDash() {
        assertEquals("—", LocationUtils.formatElevation(-100.0))
    }

    @Test
    fun formatElevation_zero_returnsZero() {
        val result = LocationUtils.formatElevation(0.0)
        assertTrue(result.contains("0.0 m"), "Got: $result")
    }

    @Test
    fun formatElevation_metresOnly() {
        assertEquals("1234.5 m", LocationUtils.formatElevation(1234.5, ElevationUnit.METRES))
    }

    @Test
    fun formatElevation_feetOnly() {
        val result = LocationUtils.formatElevation(1000.0, ElevationUnit.FEET)
        assertTrue(result.startsWith("3280"), "Expected ~3280 ft, got '$result'")
        assertTrue(result.endsWith(" ft"), "Expected to end with ' ft', got '$result'")
    }

    @Test
    fun formatElevation_null_unitSafe() {
        assertEquals("—", LocationUtils.formatElevation(null, ElevationUnit.METRES))
        assertEquals("—", LocationUtils.formatElevation(null, ElevationUnit.FEET))
    }
}
