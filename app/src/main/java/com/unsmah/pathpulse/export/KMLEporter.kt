package com.unsmah.pathpulse.export

import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object KMLEporter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    fun export(track: Track, points: List<TrackPoint>, outputFile: File): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
                writer.write("  <Document>\n")
                writer.write("    <name>${escapeXml(track.name)}</name>\n")
                writer.write("    <description>PathPulse Track Export</description>\n")
                writer.write("    <Style id=\"track-style\">\n")
                writer.write("      <LineStyle>\n")
                writer.write("        <color>ff0000ff</color>\n")
                writer.write("        <width>4</width>\n")
                writer.write("      </LineStyle>\n")
                writer.write("    </Style>\n")
                writer.write("    <Placemark>\n")
                writer.write("      <name>${escapeXml(track.name)}</name>\n")
                writer.write("      <styleUrl>#track-style</styleUrl>\n")
                writer.write("      <LineString>\n")
                writer.write("        <extrude>1</extrude>\n")
                writer.write("        <tessellate>1</tessellate>\n")
                writer.write("        <altitudeMode>clampToGround</altitudeMode>\n")
                writer.write("        <coordinates>\n")
                
                points.forEach { point ->
                    val coord = buildString {
                        append(point.longitude)
                        append(",")
                        append(point.latitude)
                        point.altitude?.let { alt ->
                            append(",")
                            append(alt)
                        }
                        append("\n")
                    }
                    writer.write("          $coord")
                }
                
                writer.write("        </coordinates>\n")
                writer.write("      </LineString>\n")
                writer.write("    </Placemark>\n")
                writer.write("  </Document>\n")
                writer.write("</kml>\n")
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
