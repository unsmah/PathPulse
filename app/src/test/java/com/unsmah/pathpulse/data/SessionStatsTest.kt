package com.unsmah.pathpulse.data

import com.unsmah.pathpulse.data.entity.TrackPoint
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for session statistics calculations in TrackMath:
 * moving time, average speed, max speed, and elevation changes.
 */
class SessionStatsTest {

    private fun point(
        lat: Double,
        lon: Double,
        ts: Long,
        alt: Double? = null,
        speed: Double? = null
    ) = TrackPoint(
        trackId = 1,
        latitude = lat,
        longitude = lon,
        altitude = alt,
        accuracy = 5.0f,
        timestamp = ts,
        speed = speed
    )

    // -----------------------------------------------------------------------
    // estimateMovingTime
    // -----------------------------------------------------------------------

    @Test
    fun estimateMovingTime_emptyList_returnsZero() {
        assertEquals(0L, TrackMath.estimateMovingTime(emptyList()))
    }

    @Test
    fun estimateMovingTime_singlePoint_returnsZero() {
        assertEquals(0L, TrackMath.estimateMovingTime(listOf(point(48.8566, 2.3522, 0L, speed = 1.0))))
    }

    @Test
    fun estimateMovingTime_twoPoints_immobile_returnsZero() {
        val points = listOf(point(48.8566, 2.3522, 0L), point(48.8566, 2.3522, 10_000L))
        assertEquals(0L, TrackMath.estimateMovingTime(points))
    }

    @Test
    fun estimateMovingTime_twoPoints_moving_returnsFullInterval() {
        // ~14 m in 10 s = 1.4 m/s > 0.5 threshold
        val points = listOf(point(48.8566, 2.3522, 0L), point(48.8575, 2.3531, 10_000L))
        assertEquals(10_000L, TrackMath.estimateMovingTime(points))
    }

    @Test
    fun estimateMovingTime_speedJustAboveThreshold_countsMoving() {
        // ~6 m in 10 s = 0.6 m/s > 0.5
        val points = listOf(point(48.0, 2.0, 0L), point(48.000054, 2.0, 10_000L))
        assertTrue(TrackMath.estimateMovingTime(points) > 0L)
    }

    @Test
    fun estimateMovingTime_speedJustBelowThreshold_countsStopped() {
        // ~4 m in 10 s = 0.4 m/s < 0.5
        val points = listOf(point(48.0, 2.0, 0L), point(48.000036, 2.0, 10_000L))
        assertEquals(0L, TrackMath.estimateMovingTime(points))
    }

    @Test
    fun estimateMovingTime_customThreshold_respected() {
        // ~10 m in 10 s = 1 m/s
        val points = listOf(point(0.0, 0.0, 0L), point(0.00009, 0.0, 10_000L))
        assertEquals(10_000L, TrackMath.estimateMovingTime(points, speedThresholdMps = 0.5))
        assertEquals(0L, TrackMath.estimateMovingTime(points, speedThresholdMps = 2.0))
    }

    @Test
    fun estimateMovingTime_alternatingMovingAndStopped() {
        val points = listOf(
            point(0.0, 0.0, 0L),
            point(0.00009, 0.0, 5_000L),   // moving 5 s
            point(0.00009, 0.0, 10_000L),  // stopped 5 s
            point(0.00018, 0.0, 15_000L),  // moving 5 s
            point(0.00018, 0.0, 20_000L)   // stopped 5 s
        )
        assertEquals(10_000L, TrackMath.estimateMovingTime(points))
    }

    // -----------------------------------------------------------------------
    // calculateAvgSpeed
    // -----------------------------------------------------------------------

    @Test
    fun calculateAvgSpeed_zeroDuration_returnsZero() {
        assertEquals(0.0, TrackMath.calculateAvgSpeed(1000.0, 0L), 0.001)
    }

    @Test
    fun calculateAvgSpeed_typicalValues() {
        // 25 km in 1 h = 25000 m / 3600 s ~= 6.94 m/s
        assertEquals(6.94, TrackMath.calculateAvgSpeed(25_000.0, 3_600_000L), 0.01)
    }

    @Test
    fun calculateAvgSpeed_zeroDistance_returnsZero() {
        assertEquals(0.0, TrackMath.calculateAvgSpeed(0.0, 60_000L), 0.001)
    }

    // -----------------------------------------------------------------------
    // findMaxSpeed
    // -----------------------------------------------------------------------

    @Test
    fun findMaxSpeed_emptyList_returnsZero() {
        assertEquals(0.0, TrackMath.findMaxSpeed(emptyList()), 0.001)
    }

    @Test
    fun findMaxSpeed_allNullSpeeds_returnsZero() {
        val points = listOf(point(0.0, 0.0, 0L), point(0.0, 0.0, 1000L))
        assertEquals(0.0, TrackMath.findMaxSpeed(points), 0.001)
    }

    @Test
    fun findMaxSpeed_mixedSpeeds_returnsMaximum() {
        val points = listOf(
            point(0.0, 0.0, 0L, speed = 5.0),
            point(0.0, 0.0, 1000L, speed = 12.5),
            point(0.0, 0.0, 2000L, speed = 8.0),
            point(0.0, 0.0, 3000L, speed = 3.0)
        )
        assertEquals(12.5, TrackMath.findMaxSpeed(points), 0.001)
    }

    @Test
    fun findMaxSpeed_allSameSpeed_returnsThatSpeed() {
        val points = listOf(
            point(0.0, 0.0, 0L, speed = 7.5),
            point(0.0, 0.0, 1000L, speed = 7.5),
            point(0.0, 0.0, 2000L, speed = 7.5)
        )
        assertEquals(7.5, TrackMath.findMaxSpeed(points), 0.001)
    }

    // -----------------------------------------------------------------------
    // calculateElevationChange
    // -----------------------------------------------------------------------

    @Test
    fun calculateElevationChange_emptyList_returnsZero() {
        val (gain, loss) = TrackMath.calculateElevationChange(emptyList())
        assertEquals(0.0, gain, 0.001)
        assertEquals(0.0, loss, 0.001)
    }

    @Test
    fun calculateElevationChange_nullAltitudes_ignored() {
        val points = listOf(point(0.0, 0.0, 0L), point(0.0, 0.0, 1000L))
        val (gain, loss) = TrackMath.calculateElevationChange(points)
        assertEquals(0.0, gain, 0.001)
        assertEquals(0.0, loss, 0.001)
    }

    @Test
    fun calculateElevationChange_uphill_countsGainOnly() {
        val points = listOf(
            point(0.0, 0.0, 0L, alt = 100.0),
            point(0.0, 0.0, 1000L, alt = 150.0),
            point(0.0, 0.0, 2000L, alt = 200.0)
        )
        val (gain, loss) = TrackMath.calculateElevationChange(points)
        assertEquals(100.0, gain, 0.001)
        assertEquals(0.0, loss, 0.001)
    }

    @Test
    fun calculateElevationChange_downhill_countsLossOnly() {
        val points = listOf(
            point(0.0, 0.0, 0L, alt = 200.0),
            point(0.0, 0.0, 1000L, alt = 150.0),
            point(0.0, 0.0, 2000L, alt = 100.0)
        )
        val (gain, loss) = TrackMath.calculateElevationChange(points)
        assertEquals(0.0, gain, 0.001)
        assertEquals(100.0, loss, 0.001)
    }

    @Test
    fun calculateElevationChange_mixed_upAndDown() {
        val points = listOf(
            point(0.0, 0.0, 0L, alt = 100.0),
            point(0.0, 0.0, 1000L, alt = 200.0),
            point(0.0, 0.0, 2000L, alt = 100.0)
        )
        val (gain, loss) = TrackMath.calculateElevationChange(points)
        assertEquals(100.0, gain, 0.001)
        assertEquals(100.0, loss, 0.001)
    }
}
