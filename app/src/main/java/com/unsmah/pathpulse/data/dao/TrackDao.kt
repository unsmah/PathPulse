package com.unsmah.pathpulse.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unsmah.pathpulse.data.entity.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY startTime DESC")
    fun getAllTracks(): Flow<List<Track>>

    @Query("SELECT * FROM tracks WHERE isActive = 1 LIMIT 1")
    fun getActiveTrack(): Flow<Track?>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): Track?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: Track): Long

    @Update
    suspend fun updateTrack(track: Track)

    @Query("UPDATE tracks SET isActive = 0 WHERE isActive = 1")
    suspend fun clearActiveTrack()

    @Delete
    suspend fun deleteTrack(track: Track)

    @Query("DELETE FROM tracks WHERE id = :id")
    suspend fun deleteTrackById(id: Long)

    @Query("SELECT * FROM tracks WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestActiveOrRecentTrack(): Track?

    @Query("SELECT COUNT(*) FROM tracks")
    suspend fun getTrackCount(): Int

    @Query("DELETE FROM tracks")
    suspend fun deleteAllTracks()
}
