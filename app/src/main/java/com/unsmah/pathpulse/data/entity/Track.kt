package com.unsmah.pathpulse.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class Track(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val startTime: Long,
    val endTime: Long? = null,
    val distanceMeters: Double = 0.0,
    val durationMillis: Long = 0,
    val movingMillis: Long = 0,
    val pausedMillis: Long = 0,
    val pointCount: Int = 0,
    val avgSpeedMps: Double = 0.0,
    val maxSpeedMps: Double = 0.0,
    val elevationGain: Double = 0.0,
    val elevationLoss: Double = 0.0,
    val isActive: Boolean = false,
    val mapEngine: String = "maplibre",
    val tileLayer: String = "osm",
    val settingsJson: String = "{}")
