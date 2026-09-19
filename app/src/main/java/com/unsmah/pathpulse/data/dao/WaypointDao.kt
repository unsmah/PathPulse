package com.unsmah.pathpulse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unsmah.pathpulse.data.entity.Waypoint
import kotlinx.coroutines.flow.Flow

@Dao
interface WaypointDao {
    @Query("SELECT * FROM waypoints WHERE trackId IS NULL OR trackId = :trackId ORDER BY timestamp ASC")
    fun getWaypoints(trackId: Long? = null): Flow<List<Waypoint>>

    @Query("SELECT * FROM waypoints WHERE id = :id")
    suspend fun getWaypointById(id: Long): Waypoint?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(waypoint: Waypoint): Long

    @Update
    suspend fun updateWaypoint(waypoint: Waypoint)

    @Delete
    suspend fun deleteWaypoint(waypoint: Waypoint)

    @Query("DELETE FROM waypoints WHERE id = :id")
    suspend fun deleteWaypointById(id: Long)

    @Query("DELETE FROM waypoints WHERE trackId = :trackId")
    suspend fun deleteWaypointsForTrack(trackId: Long)

    @Query("DELETE FROM waypoints")
    suspend fun deleteAllWaypoints()
}
