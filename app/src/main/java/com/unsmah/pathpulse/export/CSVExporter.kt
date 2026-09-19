package com.unsmah.pathpulse.export

import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CSVExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun export(track: Track, points: List<TrackPoint>, outputFile: File): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                // Header
                writer.write("id,latitude,longitude,altitude,accuracy,speed,bearing,timestamp\n")
                
                // Data rows
                points.forEach { point ->
                    writer.write("${point.id},${point.latitude},${point.longitude}")
                    point.altitude?.let { alt ->
                        writer.write(",$alt")
                    } ?: writer.write(",")
                    writer.write(",${point.accuracy}")
                    point.speed?.let { speed ->
                        writer.write(",$speed")
                    } ?: writer.write(",")
                    point.bearing?.let { bearing ->
                        writer.write(",$bearing")
                    } ?: writer.write(",")
                    writer.write(",${dateFormat.format(Date(point.timestamp))}\n")
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}
