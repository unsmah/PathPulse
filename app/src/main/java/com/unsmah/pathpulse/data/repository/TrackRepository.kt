package com.unsmah.pathpulse.data.repository

import com.unsmah.pathpulse.data.dao.GeofenceDao
import com.unsmah.pathpulse.data.dao.TrackDao
import com.unsmah.pathpulse.data.dao.TrackPointDao
import com.unsmah.pathpulse.data.dao.WaypointDao
import com.unsmah.pathpulse.data.entity.Geofence
import com.unsmah.pathpulse.data.entity.GeofenceEvent
import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import com.unsmah.pathpulse.data.entity.Waypoint
import kotlinx.coroutines.flow.Flow

class TrackRepository(
    private val trackDao: TrackDao,
    private val trackPointDao: TrackPointDao,
    private val waypointDao: WaypointDao,
    private val geofenceDao: GeofenceDao
) {
    val allTracks: Flow<List<Track>> = trackDao.getAllTracks()
    val activeTrack: Flow<Track?> = trackDao.getActiveTrack()

    suspend fun getTrack(id: Long): Track? = trackDao.getTrackById(id)
    suspend fun getLatestTrack(): Track? = trackDao.getLatestActiveOrRecentTrack()

    suspend fun startTrack(name: String, mapEngine: String = "maplibre", tileLayer: String = "osm"): Long {
        trackDao.clearActiveTrack()
        val track = Track(
            name = name,
            startTime = System.currentTimeMillis(),
            isActive = true,
            mapEngine = mapEngine,
            tileLayer = tileLayer
        )
        return trackDao.insertTrack(track)
    }

    suspend fun endTrack(trackId: Long, endTime: Long = System.currentTimeMillis()) {
        val track = trackDao.getTrackById(trackId)
        if (track != null) {
            val points = trackPointDao.getPointsForTrackSync(trackId)
            val distance = calculateDistance(points)
            val duration = endTime - track.startTime
            val movingTime = estimateMovingTime(points)
            val avgSpeed = if (duration > 0) distance / (duration / 1000.0) else 0.0
            val maxSpeed = points.maxOfOrNull { it.speed ?: 0.0 } ?: 0.0
            val (gain, loss) = calculateElevationChange(points)

            trackDao.updateTrack(track.copy(
                endTime = endTime,
                distanceMeters = distance,
                durationMillis = duration,
                movingMillis = movingTime,
                pointCount = points.size,
                avgSpeedMps = avgSpeed,
                maxSpeedMps = maxSpeed,
                elevationGain = gain,
                elevationLoss = loss,
                isActive = false
            ))
        }
    }

    suspend fun addPoint(point: TrackPoint): Long = trackPointDao.insertPoint(point)
    suspend fun addPoints(points: List<TrackPoint>) = trackPointDao.insertPoints(points)

    fun getPointsForTrack(trackId: Long): Flow<List<TrackPoint>> = trackPointDao.getPointsForTrack(trackId)
    suspend fun getPointsForTrackSync(trackId: Long): List<TrackPoint> = trackPointDao.getPointsForTrackSync(trackId)
    suspend fun getLastPoint(trackId: Long): TrackPoint? = trackPointDao.getLastPointForTrack(trackId)
    suspend fun getPointCount(trackId: Long): Int = trackPointDao.getPointCountForTrack(trackId)

    suspend fun addWaypoint(waypoint: Waypoint): Long = waypointDao.insertWaypoint(waypoint)
    fun getWaypoints(trackId: Long? = null): Flow<List<Waypoint>> = waypointDao.getWaypoints(trackId)
    suspend fun deleteWaypoint(waypoint: Waypoint) = waypointDao.deleteWaypoint(waypoint)

    // Geofences
    val activeGeofences: Flow<List<Geofence>> = geofenceDao.getActiveGeofences()
    val allGeofences: Flow<List<Geofence>> = geofenceDao.getAllGeofences()

    suspend fun addGeofence(geofence: Geofence): Long = geofenceDao.insertGeofence(geofence)
    suspend fun updateGeofence(geofence: Geofence) = geofenceDao.updateGeofence(geofence)
    suspend fun deleteGeofence(geofence: Geofence) = geofenceDao.deleteGeofence(geofence)
    suspend fun toggleGeofenceActive(id: Long, active: Boolean) = geofenceDao.setGeofenceActive(id, active)

    suspend fun addGeofenceEvent(event: GeofenceEvent): Long = geofenceDao.insertGeofenceEvent(event)
    fun getEventsForGeofence(geofenceId: Long): Flow<List<GeofenceEvent>> = geofenceDao.getEventsForGeofence(geofenceId)

    suspend fun deleteTrack(track: Track) {
        trackPointDao.deletePointsForTrack(track.id)
        waypointDao.deleteWaypointsForTrack(track.id)
        trackDao.deleteTrack(track)
    }

    suspend fun deleteTrackById(id: Long) {
        trackPointDao.deletePointsForTrack(id)
        waypointDao.deleteWaypointsForTrack(id)
        trackDao.deleteTrackById(id)
    }

    suspend fun deleteAllData() {
        trackDao.deleteAllTracks()
        trackPointDao.deleteAllPoints()
        waypointDao.deleteAllWaypoints()
        geofenceDao.deleteAllGeofences()
        geofenceDao.deleteAllGeofenceEvents()
    }

    private fun calculateDistance(points: List<TrackPoint>): Double {
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

    private fun estimateMovingTime(points: List<TrackPoint>): Long {
        if (points.size < 2) return 0L
        var moving = 0L
        for (i in 1 until points.size) {
            val dt = points[i].timestamp - points[i - 1].timestamp
            val dist = haversine(
                points[i - 1].latitude, points[i - 1].longitude,
                points[i].latitude, points[i].longitude
            )
            val speed = if (dt > 0) dist / (dt / 1000.0) else 0.0
            if (speed > 0.5) { // > 0.5 m/s = ~1.8 km/h, walking threshold
                moving += dt
            }
        }
        return moving
    }

    private fun calculateElevationChange(points: List<TrackPoint>): Pair<Double, Double> {
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

    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}
