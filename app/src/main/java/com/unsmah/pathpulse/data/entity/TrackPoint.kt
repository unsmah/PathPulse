package com.unsmah.pathpulse.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "track_points",
    foreignKeys = [ForeignKey(
        entity = Track::class,
        parentColumns = ["id"],
        childColumns = ["trackId"],
        onDelete = androidx.room.ForeignKey.CASCADE
    )],
    indices = [Index("trackId")]
)
data class TrackPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: Long,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double? = null,
    val accuracy: Float,
    val speed: Double? = null,
    val bearing: Float? = null,
    val timestamp: Long
)
