package com.unsmah.pathpulse.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.unsmah.pathpulse.data.entity.TrackPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackPointDao {
    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY timestamp ASC")
    fun getPointsForTrack(trackId: Long): Flow<List<TrackPoint>>

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY timestamp ASC")
    suspend fun getPointsForTrackSync(trackId: Long): List<TrackPoint>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: TrackPoint): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoints(points: List<TrackPoint>)

    @Query("DELETE FROM track_points WHERE trackId = :trackId")
    suspend fun deletePointsForTrack(trackId: Long)

    @Query("SELECT COUNT(*) FROM track_points WHERE trackId = :trackId")
    suspend fun getPointCountForTrack(trackId: Long): Int

    @Query("SELECT * FROM track_points WHERE trackId = :trackId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastPointForTrack(trackId: Long): TrackPoint?

    @Query("DELETE FROM track_points")
    suspend fun deleteAllPoints()
}
