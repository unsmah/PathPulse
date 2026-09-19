package com.unsmah.pathpulse.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "geofence_events",
    foreignKeys = [
        ForeignKey(entity = Geofence::class, parentColumns = ["id"], childColumns = ["geofenceId"], onDelete = androidx.room.ForeignKey.CASCADE),
        ForeignKey(entity = Track::class, parentColumns = ["id"], childColumns = ["trackId"], onDelete = androidx.room.ForeignKey.SET_NULL)
    ],
    indices = [Index("geofenceId"), Index("trackId")]
)
data class GeofenceEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val geofenceId: Long,
    val trackId: Long?,
    val eventType: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double
)
