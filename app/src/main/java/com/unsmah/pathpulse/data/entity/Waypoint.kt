package com.unsmah.pathpulse.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "waypoints",
    foreignKeys = [ForeignKey(
        entity = Track::class,
        parentColumns = ["id"],
        childColumns = ["trackId"],
        onDelete = androidx.room.ForeignKey.SET_NULL
    )],
    indices = [Index("trackId")]
)
data class Waypoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val trackId: Long?,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val note: String? = null,
    val hasPhoto: Boolean = false,
    val timestamp: Long
)
