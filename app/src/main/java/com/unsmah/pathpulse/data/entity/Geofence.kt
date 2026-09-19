package com.unsmah.pathpulse.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofences")
data class Geofence(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val notifyEnter: Boolean = true,
    val notifyExit: Boolean = true,
    val notifyDwell: Boolean = false,
    val dwellSeconds: Int = 10,
    val isActive: Boolean = true
)
