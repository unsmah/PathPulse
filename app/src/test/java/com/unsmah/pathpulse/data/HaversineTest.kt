package com.unsmah.pathpulse.data

import com.unsmah.pathpulse.data.entity.TrackPoint
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for the Haversine distance calculation in TrackMath.
 */
class HaversineTest {

    @Test
    fun haversine_samePoint_returnsZero() {
        assertEquals(0.0, TrackMath.haversine(48.8566, 2.3522, 48.8566, 2.3522), 0.001)
    }

    @Test
    fun haversine_zeroLengthSegment_returnsZero() {
        assertEquals(0.0, TrackMath.haversine(0.0, 0.0, 0.0, 0.0), 0.001)
    }

    @Test
    fun haversine_parisToLondon_approxCorrect() {
        val d = TrackMath.haversine(48.8566, 2.3522, 51.5074, -0.1278)
        assertTrue("Expected ~343000m, got ${d}m", d in 342900.0..343100.0)
    }

    @Test
    fun haversine_newYorkToLosAngeles_approxCorrect() {
        val d = TrackMath.haversine(40.7128, -74.0060, 34.0522, -118.2437)
        assertTrue("Expected ~3940000m, got ${d}m", d in 3935000.0..3945000.0)
    }

    @Test
    fun haversine_equator_1DegreeLongitude() {
        val d = TrackMath.haversine(0.0, 0.0, 0.0, 1.0)
        assertTrue("Expected ~111320m at equator, got ${d}m", d in 111200.0..111400.0)
    }

    @Test
    fun haversine_1DegreeLatitude() {
        val d = TrackMath.haversine(0.0, 0.0, 1.0, 0.0)
        assertTrue("Expected ~111320m, got ${d}m", d in 111200.0..111400.0)
    }

    @Test
    fun haversine_60N_1DegreeLongitude_halfOfEquator() {
        val d = TrackMath.haversine(60.0, 0.0, 60.0, 1.0)
        assertTrue("Expected ~55800m at 60°N, got ${d}m", d in 55700.0..55900.0)
    }

    @Test
    fun haversine_antimeridian_shortPath() {
        val d = TrackMath.haversine(0.0, 179.9, 0.0, -179.9)
        assertTrue("Should be small across antimeridian, got ${d}m", d < 250.0)
    }

    @Test
    fun haversine_antipodal_oppositeSidesOfEarth() {
        val d = TrackMath.haversine(0.0, 0.0, 0.0, 180.0)
        assertTrue("Expected ~20037000m, got ${d}m", d in 20030000.0..20040000.0)
    }

    @Test
    fun haversine_smallOffset_10Meters() {
        val d = TrackMath.haversine(45.0, 0.0, 45.00009, 0.0)
        assertTrue("Expected ~10m, got ${d}m", d in 9.0..11.0)
    }

    @Test
    fun haversine_negativeCoordinates() {
        val d = TrackMath.haversine(-33.8688, 151.2093, -34.6037, -58.3816)
        assertTrue("Expected ~11900000m, got ${d}m", d in 11890000.0..11910000.0)
    }

    @Test
    fun haversine_symmetric() {
        val d1 = TrackMath.haversine(48.8566, 2.3522, 51.5074, -0.1278)
        val d2 = TrackMath.haversine(51.5074, -0.1278, 48.8566, 2.3522)
        assertEquals(d1, d2, 0.001)
    }

    @Test
    fun calculateDistance_emptyList_returnsZero() {
        assertEquals(0.0, TrackMath.calculateDistance(emptyList()), 0.001)
    }

    @Test
    fun calculateDistance_singlePoint_returnsZero() {
        val p = TrackPoint(trackId = 1, latitude = 48.8566, longitude = 2.3522,
            altitude = null, accuracy = 5.0f, timestamp = System.currentTimeMillis())
        assertEquals(0.0, TrackMath.calculateDistance(listOf(p)), 0.001)
    }

    @Test
    fun calculateDistance_twoIdenticalPoints_returnsZero() {
        val ts = System.currentTimeMillis()
        val points = listOf(
            TrackPoint(trackId = 1, latitude = 48.8566, longitude = 2.3522,
                altitude = null, accuracy = 5.0f, timestamp = ts),
            TrackPoint(trackId = 1, latitude = 48.8566, longitude = 2.3522,
                altitude = null, accuracy = 5.0f, timestamp = ts + 1000)
        )
        assertEquals(0.0, TrackMath.calculateDistance(points), 0.001)
    }

    @Test
    fun calculateDistance_parisLondon() {
        val ts = System.currentTimeMillis()
        val points = listOf(
            TrackPoint(trackId = 1, latitude = 48.8566, longitude = 2.3522,
                altitude = null, accuracy = 5.0f, timestamp = ts),
            TrackPoint(trackId = 1, latitude = 51.5074, longitude = -0.1278,
                altitude = null, accuracy = 5.0f, timestamp = ts + 1000)
        )
        val d = TrackMath.calculateDistance(points)
        assertTrue("Expected ~343000m, got ${d}m", d in 342900.0..343100.0)
    }

    @Test
    fun calculateDistance_parisLondonBerlin() {
        val ts = System.currentTimeMillis()
        val points = listOf(
            TrackPoint(trackId = 1, latitude = 48.8566, longitude = 2.3522,
                altitude = null, accuracy = 5.0f, timestamp = ts),
            TrackPoint(trackId = 1, latitude = 51.5074, longitude = -0.1278,
                altitude = null, accuracy = 5.0f, timestamp = ts + 1000),
            TrackPoint(trackId = 1, latitude = 52.5200, longitude = 13.4050,
                altitude = null, accuracy = 5.0f, timestamp = ts + 2000)
        )
        val d = TrackMath.calculateDistance(points)
        assertTrue("Expected ~1273000m total, got ${d}m", d in 1272000.0..1274000.0)
    }

    @Test
    fun haversine_scaleWithLatitude_cosineEffect() {
        val equator = TrackMath.haversine(0.0, 0.0, 0.0, 1.0)
        val at60N = TrackMath.haversine(60.0, 0.0, 60.0, 1.0)
        assertTrue(
            "60°N distance should be roughly half of equator: ${at60N} vs ${equator}",
            at60N < equator * 0.6 && at60N > equator * 0.4
        )
    }
}