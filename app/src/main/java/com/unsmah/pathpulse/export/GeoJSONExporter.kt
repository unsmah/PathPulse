package com.unsmah.pathpulse.export

import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GeoJSONExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    fun export(track: Track, points: List<TrackPoint>, outputFile: File): Boolean {
        return try {
            val features = JSONArray()
            val coordinates = JSONArray()
            
            points.forEach { point ->
                val coords = JSONArray()
                coords.put(point.longitude)
                coords.put(point.latitude)
                point.altitude?.let { alt ->
                    coords.put(alt)
                }
                coordinates.put(coords)
            }

            val geometry = JSONObject().apply {
                put("type", "LineString")
                put("coordinates", coordinates)
            }

            val properties = JSONObject().apply {
                put("name", track.name)
                put("distanceMeters", track.distanceMeters)
                put("durationMillis", track.durationMillis)
                put("pointCount", track.pointCount)
                put("avgSpeedMps", track.avgSpeedMps)
                put("maxSpeedMps", track.maxSpeedMps)
                put("elevationGain", track.elevationGain)
                put("elevationLoss", track.elevationLoss)
                put("startTime", dateFormat.format(Date(track.startTime)))
            }

            val feature = JSONObject().apply {
                put("type", "Feature")
                put("geometry", geometry)
                put("properties", properties)
            }
            features.put(feature)

            val featureCollection = JSONObject().apply {
                put("type", "FeatureCollection")
                put("features", features)
            }

            FileWriter(outputFile).use { writer ->
                writer.write(featureCollection.toString(2))
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
