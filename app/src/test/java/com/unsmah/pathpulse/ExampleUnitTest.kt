package com.unsmah.pathpulse

import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import com.unsmah.pathpulse.data.repository.TrackRepository
import com.unsmah.pathpulse.export.GPXExporter
import com.unsmah.pathpulse.export.GeoJSONExporter
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File

/**
 * Example unit tests for PathPulse.
 * Tests the haversine distance calculation, GPX exporter, and GeoJSON exporter.
 */
class ExampleUnitTest {

    // -----------------------------------------------------------------------
    // Haversine distance calculation (via TrackRepository)
    // -----------------------------------------------------------------------

    @Test
    fun testHaversineDistance_zeroPoints_returnsZero() = runTest {
        val repo = createTestRepository()
        val points = emptyList<TrackPoint>()
        val distance = invokeHaversine(repo, points)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testHaversineDistance_singlePoint_returnsZero() = runTest {
        val repo = createTestRepository()
        val points = listOf(
            TrackPoint(latitude = 48.8566, longitude = 2.3522, timestamp = System.currentTimeMillis())
        )
        val distance = invokeHaversine(repo, points)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testHaversineDistance_twoIdenticalPoints_returnsZero() = runTest {
        val repo = createTestRepository()
        val ts = System.currentTimeMillis()
        val points = listOf(
            TrackPoint(latitude = 48.8566, longitude = 2.3522, timestamp = ts),
            TrackPoint(latitude = 48.8566, longitude = 2.3522, timestamp = ts + 1000)
        )
        val distance = invokeHaversine(repo, points)
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testHaversineDistance_parisToLondon_approxCorrect() = runTest {
        val repo = createTestRepository()
        val ts = System.currentTimeMillis()
        val paris = TrackPoint(latitude = 48.8566, longitude = 2.3522, timestamp = ts)
        val london = TrackPoint(latitude = 51.5074, longitude = -0.1278, timestamp = ts + 1000)
        val points = listOf(paris, london)

        val distance = invokeHaversine(repo, points)
        assertTrue(
            "Expected distance ~343000m, got ${distance}m",
            distance in 342900.0..343100.0
        )
    }

    @Test
    fun testHaversineDistance_antimeridian_notExploding() = runTest {
        val repo = createTestRepository()
        val ts = System.currentTimeMillis()
        val p1 = TrackPoint(latitude = 0.0, longitude = 179.9, timestamp = ts)
        val p2 = TrackPoint(latitude = 0.0, longitude = -179.9, timestamp = ts + 1000)
        val points = listOf(p1, p2)

        val distance = invokeHaversine(repo, points)
        assertTrue("Distance should be small across antimeridian, got ${distance}m", distance < 1000.0)
    }

    // -----------------------------------------------------------------------
    // GPX Exporter
    // -----------------------------------------------------------------------

    @Test
    fun testGPXExporter_validOutput_formatCorrect() = runTest {
        val track = Track(
            id = 1, name = "Test Track", startTime = System.currentTimeMillis(),
            isActive = false, distanceMeters = 1000.0, durationMillis = 60000L,
            pointCount = 3, mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(
            TrackPoint(latitude = 48.8566, longitude = 2.3522, altitude = 35.0, timestamp = System.currentTimeMillis()),
            TrackPoint(latitude = 48.8570, longitude = 2.3530, altitude = 36.0, timestamp = System.currentTimeMillis() + 1000),
            TrackPoint(latitude = 48.8575, longitude = 2.3540, altitude = 37.0, timestamp = System.currentTimeMillis() + 2000)
        )

        val outputFile = File.createTempFile("test", ".gpx")
        outputFile.deleteOnExit()

        val success = GPXExporter.export(track, points, outputFile)
        assertTrue("GPX export should succeed", success)

        val content = outputFile.readText()
        assertTrue("Should start with XML declaration", content.startsWith("<?xml"))
        assertTrue("Should contain GPX tag", content.contains("<gpx"))
        assertTrue("Should contain trk tag", content.contains("<trk>"))
        assertTrue("Should contain trkseg tag", content.contains("<trkseg>"))
        assertTrue("Should contain track name", content.contains("Test Track"))
        assertTrue("Should contain trkpt elements", content.contains("<trkpt"))
        assertTrue("Should contain elevation element", content.contains("<ele>35.0</ele>"))
        assertTrue("Should contain time elements", content.contains("<time>"))

        outputFile.delete()
    }

    @Test
    fun testGPXExporter_gpxVersionAndCreator() = runTest {
        val track = Track(
            id = 1, name = "Version Test", startTime = System.currentTimeMillis(),
            isActive = false, distanceMeters = 0.0, durationMillis = 0L,
            pointCount = 1, mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(TrackPoint(latitude = 0.0, longitude = 0.0, timestamp = System.currentTimeMillis()))

        val outputFile = File.createTempFile("test", ".gpx")
        outputFile.deleteOnExit()

        GPXExporter.export(track, points, outputFile)
        val content = outputFile.readText()

        assertTrue("Should have GPX version 1.1", content.contains("version=\"1.1\""))
        assertTrue("Should have creator PathPulse", content.contains("creator=\"PathPulse\""))
        outputFile.delete()
    }

    @Test
    fun testGPXExporter_xmlEscaping() = runTest {
        val track = Track(
            id = 1, name = "Test & <Track> \"Quote\" 'Apostrophe'",
            startTime = System.currentTimeMillis(), isActive = false,
            distanceMeters = 0.0, durationMillis = 0L, pointCount = 1,
            mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(TrackPoint(latitude = 0.0, longitude = 0.0, timestamp = System.currentTimeMillis()))

        val outputFile = File.createTempFile("test", ".gpx")
        outputFile.deleteOnExit()

        GPXExporter.export(track, points, outputFile)
        val content = outputFile.readText()

        assertTrue("Should escape ampersand", content.contains("&amp;"))
        assertTrue("Should escape less-than", content.contains("&lt;"))
        assertTrue("Should escape greater-than", content.contains("&gt;"))
        assertTrue("Should escape quotes", content.contains("&quot;"))
        assertTrue("Should escape apostrophe", content.contains("&apos;"))
        outputFile.delete()
    }

    @Test
    fun testGPXExporter_noAltitude_omitsEleTag() = runTest {
        val track = Track(
            id = 1, name = "No Alt Track", startTime = System.currentTimeMillis(),
            isActive = false, distanceMeters = 0.0, durationMillis = 0L,
            pointCount = 1, mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(TrackPoint(latitude = 48.8566, longitude = 2.3522, altitude = null, timestamp = System.currentTimeMillis()))

        val outputFile = File.createTempFile("test", ".gpx")
        outputFile.deleteOnExit()

        GPXExporter.export(track, points, outputFile)
        val content = outputFile.readText()
        assertFalse("Should not contain ele tag when altitude is null", content.contains("<ele>"))
        outputFile.delete()
    }

    // -----------------------------------------------------------------------
    // GeoJSON Exporter
    // -----------------------------------------------------------------------

    @Test
    fun testGeoJSONExporter_validOutput_isValidJson() = runTest {
        val track = Track(
            id = 1, name = "GeoJSON Test", startTime = System.currentTimeMillis(),
            isActive = false, distanceMeters = 5000.0, durationMillis = 120000L,
            pointCount = 3, avgSpeedMps = 1.5, maxSpeedMps = 3.2,
            elevationGain = 150.0, elevationLoss = 80.0,
            mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(
            TrackPoint(latitude = 48.8566, longitude = 2.3522, altitude = 35.0, timestamp = System.currentTimeMillis()),
            TrackPoint(latitude = 48.8570, longitude = 2.3530, altitude = 36.0, timestamp = System.currentTimeMillis() + 1000),
            TrackPoint(latitude = 48.8575, longitude = 2.3540, altitude = 37.0, timestamp = System.currentTimeMillis() + 2000)
        )

        val outputFile = File.createTempFile("test", ".geojson")
        outputFile.deleteOnExit()

        val success = GeoJSONExporter.export(track, points, outputFile)
        assertTrue("GeoJSON export should succeed", success)

        val content = outputFile.readText()
        val json = JSONObject(content)

        assertEquals("FeatureCollection", json.getString("type"))
        val features = json.getJSONArray("features")
        assertEquals(1, features.length())

        val feature = features.getJSONObject(0)
        assertEquals("Feature", feature.getString("type"))

        val geometry = feature.getJSONObject("geometry")
        assertEquals("LineString", geometry.getString("type"))

        val coords = geometry.getJSONArray("coordinates")
        assertEquals(3, coords.length())

        val firstCoord = coords.getJSONArray(0)
        assertEquals(2.3522, firstCoord.getDouble(0), 0.0001)
        assertEquals(48.8566, firstCoord.getDouble(1), 0.0001)
        assertEquals(35.0, firstCoord.getDouble(2), 0.001)

        val properties = feature.getJSONObject("properties")
        assertEquals("GeoJSON Test", properties.getString("name"))
        assertEquals(5000.0, properties.getDouble("distanceMeters"), 0.001)
        assertEquals(120000L, properties.getLong("durationMillis"))
        assertEquals(3, properties.getInt("pointCount"))
        assertEquals(1.5, properties.getDouble("avgSpeedMps"), 0.001)
        assertEquals(3.2, properties.getDouble("maxSpeedMps"), 0.001)
        assertEquals(150.0, properties.getDouble("elevationGain"), 0.001)
        assertEquals(80.0, properties.getDouble("elevationLoss"), 0.001)

        outputFile.delete()
    }

    @Test
    fun testGeoJSONExporter_lineStringGeometry() = runTest {
        val track = Track(
            id = 1, name = "LineString Test", startTime = System.currentTimeMillis(),
            isActive = false, distanceMeters = 0.0, durationMillis = 0L,
            pointCount = 2, mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(
            TrackPoint(latitude = 10.0, longitude = 20.0, timestamp = System.currentTimeMillis()),
            TrackPoint(latitude = 11.0, longitude = 21.0, timestamp = System.currentTimeMillis() + 1000)
        )

        val outputFile = File.createTempFile("test", ".geojson")
        outputFile.deleteOnExit()

        GeoJSONExporter.export(track, points, outputFile)
        val json = JSONObject(outputFile.readText())
        val geometry = json.getJSONArray("features").getJSONObject(0).getJSONObject("geometry")
        assertEquals("LineString", geometry.getString("type"))
        outputFile.delete()
    }

    @Test
    fun testGeoJSONExporter_noAltitude_2dCoordinates() = runTest {
        val track = Track(
            id = 1, name = "2D Track", startTime = System.currentTimeMillis(),
            isActive = false, distanceMeters = 0.0, durationMillis = 0L,
            pointCount = 1, mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(TrackPoint(latitude = 48.8566, longitude = 2.3522, altitude = null, timestamp = System.currentTimeMillis()))

        val outputFile = File.createTempFile("test", ".geojson")
        outputFile.deleteOnExit()

        GeoJSONExporter.export(track, points, outputFile)
        val json = JSONObject(outputFile.readText())
        val coords = json.getJSONArray("features")
            .getJSONObject(0).getJSONObject("geometry")
            .getJSONArray("coordinates").getJSONArray(0)

        assertEquals(2, coords.length())
        outputFile.delete()
    }

    @Test
    fun testGeoJSONExporter_propertiesContainAllFields() = runTest {
        val startTime = System.currentTimeMillis()
        val track = Track(
            id = 1, name = "Full Properties", startTime = startTime,
            isActive = false, distanceMeters = 9999.9, durationMillis = 50000L,
            pointCount = 42, avgSpeedMps = 2.5, maxSpeedMps = 7.8,
            elevationGain = 200.0, elevationLoss = 100.0,
            mapEngine = "maplibre", tileLayer = "osm"
        )
        val points = listOf(TrackPoint(latitude = 0.0, longitude = 0.0, timestamp = startTime))

        val outputFile = File.createTempFile("test", ".geojson")
        outputFile.deleteOnExit()

        GeoJSONExporter.export(track, points, outputFile)
        val json = JSONObject(outputFile.readText())
        val props = json.getJSONArray("features").getJSONObject(0).getJSONObject("properties")

        val expectedKeys = listOf(
            "name", "distanceMeters", "durationMillis", "pointCount",
            "avgSpeedMps", "maxSpeedMps", "elevationGain", "elevationLoss", "startTime"
        )
        for (key in expectedKeys) {
            assertTrue("Properties should contain '$key'", props.has(key))
        }
        outputFile.delete()
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun createTestRepository(): TrackRepository {
        return TrackRepository(
            trackDao = null!!,
            trackPointDao = null!!,
            waypointDao = null!!,
            geofenceDao = null!!
        )
    }

    private fun invokeHaversine(repo: TrackRepository, points: List<TrackPoint>): Double {
        val method = TrackRepository::class.java.getDeclaredMethod(
            "haversine", Double::class.java, Double::class.java, Double::class.java, Double::class.java
        )
        method.isAccessible = true

        if (points.size < 2) return 0.0
        var total = 0.0
        for (i in 1 until points.size) {
            total += method.invoke(
                repo,
                points[i - 1].latitude,
                points[i - 1].longitude,
                points[i].latitude,
                points[i].longitude
            ) as Double
        }
        return total
    }
}
