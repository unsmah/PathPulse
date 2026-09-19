package com.unsmah.pathpulse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unsmah.pathpulse.data.entity.Geofence
import com.unsmah.pathpulse.data.entity.GeofenceEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {
    @Query("SELECT * FROM geofences WHERE isActive = 1 ORDER BY id ASC")
    fun getActiveGeofences(): Flow<List<Geofence>>

    @Query("SELECT * FROM geofences ORDER BY id ASC")
    fun getAllGeofences(): Flow<List<Geofence>>

    @Query("SELECT * FROM geofences WHERE id = :id")
    suspend fun getGeofenceById(id: Long): Geofence?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofence(geofence: Geofence): Long

    @Update
    suspend fun updateGeofence(geofence: Geofence)

    @Delete
    suspend fun deleteGeofence(geofence: Geofence)

    @Query("DELETE FROM geofences WHERE id = :id")
    suspend fun deleteGeofenceById(id: Long)

    @Query("UPDATE geofences SET isActive = :isActive WHERE id = :id")
    suspend fun setGeofenceActive(id: Long, isActive: Boolean)

    // Geofence events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofenceEvent(event: GeofenceEvent): Long

    @Query("SELECT * FROM geofence_events WHERE geofenceId = :geofenceId ORDER BY timestamp DESC")
    fun getEventsForGeofence(geofenceId: Long): Flow<List<GeofenceEvent>>

    @Query("DELETE FROM geofence_events")
    suspend fun deleteAllGeofenceEvents()

    @Query("DELETE FROM geofences")
    suspend fun deleteAllGeofences()
}
