package com.unsmah.pathpulse.export

import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object GPXExporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

    fun export(track: Track, points: List<TrackPoint>, outputFile: File): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                writer.write("<gpx version=\"1.1\" creator=\"PathPulse\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n")
                writer.write("  <trk>\n")
                writer.write("    <name>${escapeXml(track.name)}</name>\n")
                writer.write("    <trkseg>\n")
                points.forEach { point ->
                    writer.write("      <trkpt lat=\"${point.latitude}\" lon=\"${point.longitude}\">\n")
                    point.altitude?.let { alt ->
                        writer.write("        <ele>$alt</ele>\n")
                    }
                    writer.write("        <time>${dateFormat.format(Date(point.timestamp))}</time>\n")
                    writer.write("      </trkpt>\n")
                }
                writer.write("    </trkseg>\n")
                writer.write("  </trk>\n")
                writer.write("</gpx>\n")
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&apos;")
    }
}
