package com.unsmah.pathpulse.ui.history

import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.unsmah.pathpulse.R
import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import com.unsmah.pathpulse.export.CSVExporter
import com.unsmah.pathpulse.export.GPXExporter
import com.unsmah.pathpulse.export.GeoJSONExporter
import com.unsmah.pathpulse.export.KMLEporter
import com.unsmah.pathpulse.ui.MainViewModel
import com.unsmah.pathpulse.ui.components.LoadingState
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetailScreen(
    trackId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var track by remember { mutableStateOf<Track?>(null) }
    var points by remember { mutableStateOf<List<TrackPoint>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expandedPoints by remember { mutableStateOf(false) }
    var exportResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.detail_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LoadingState()
            }
        } else if (track == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Track not found")
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Track info header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = track!!.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val dateFormat = SimpleDateFormat("MMMM dd, yyyy \'at\' HH:mm", Locale.getDefault())
                        Text(
                            text = dateFormat.format(Date(track!!.startTime)),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Statistics
                Text(
                    text = stringResource(R.string.detail_statistics),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(
                        label = stringResource(R.string.track_distance),
                        value = formatDistance(track!!.distanceMeters),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = stringResource(R.string.track_duration),
                        value = formatDuration(track!!.durationMillis),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(
                        label = stringResource(R.string.track_avg_speed),
                        value = formatSpeed(track!!.avgSpeedMps),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = stringResource(R.string.track_points),
                        value = track!!.pointCount.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatItem(
                        label = "Max speed",
                        value = formatSpeed(track!!.maxSpeedMps),
                        modifier = Modifier.weight(1f)
                    )
                    StatItem(
                        label = "Elevation gain",
                        value = formatElevation(track!!.elevationGain),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Map preview
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.map_title),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Points section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.detail_points),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = { expandedPoints = !expandedPoints }) {
                        Text(
                            if (expandedPoints) {
                                stringResource(R.string.detail_points_expanded)
                            } else {
                                stringResource(R.string.detail_points_collapsed)
                            }
                        )
                    }
                }

                if (expandedPoints) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(points) { point ->
                            PointItem(point = point)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Export section
                Text(
                    text = stringResource(R.string.detail_export),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportButton(
                        text = stringResource(R.string.detail_export_gpx),
                        icon = Icons.Default.Download,
                        onClick = { exportAsGPX(context, track!!, points) }
                    )
                    ExportButton(
                        text = stringResource(R.string.detail_export_geojson),
                        icon = Icons.Default.Download,
                        onClick = { exportAsGeoJSON(context, track!!, points) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExportButton(
                        text = stringResource(R.string.detail_export_csv),
                        icon = Icons.Default.Download,
                        onClick = { exportAsCSV(context, track!!, points) }
                    )
                    ExportButton(
                        text = stringResource(R.string.detail_export_kml),
                        icon = Icons.Default.Download,
                        onClick = { exportAsKML(context, track!!, points) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Share button
                Button(
                    onClick = { /* Share logic */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.detail_share))
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.history_delete_confirm)) },
            text = { Text(stringResource(R.string.history_delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteTrack(context, trackId)
                        showDeleteDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    loadTrackDetails(context, trackId) { loadedTrack, loadedPoints ->
        track = loadedTrack
        points = loadedPoints
        isLoading = false
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PointItem(point: TrackPoint) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = String.format(
                "%.4f, %.4f",
                point.latitude,
                point.longitude
            ),
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = dateFormat.format(Date(point.timestamp)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ExportButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun formatDistance(meters: Double): String {
    return when {
        meters < 1000 -> String.format("%.0f m", meters)
        else -> String.format("%.2f km", meters / 1000)
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%dh %dm %ds", hours, minutes, seconds)
    } else if (minutes > 0) {
        String.format("%dm %ds", minutes, seconds)
    } else {
        String.format("%ds", seconds)
    }
}

private fun formatSpeed(mps: Double): String {
    val kmh = mps * 3.6
    return String.format("%.1f km/h", kmh)
}

private fun formatElevation(meters: Double): String {
    return String.format("%.0f m", meters)
}


private fun loadTrackDetails(
    context: android.content.Context,
    trackId: Long,
    onResult: (Track?, List<TrackPoint>) -> Unit
) {
    runBlocking {
        try {
            val db = com.unsmah.pathpulse.data.PathPulseDatabase.getInstance(context)
            val repo = com.unsmah.pathpulse.data.repository.TrackRepository(
                db.trackDao(),
                db.trackPointDao(),
                db.waypointDao(),
                db.geofenceDao()
            )
            val track = repo.getTrack(trackId)
            val points = repo.getPointsForTrackSync(trackId)
            onResult(track, points)
        } catch (e: Exception) {
            onResult(null, emptyList())
        }
    }
}

private fun deleteTrack(context: android.content.Context, trackId: Long) {
    runBlocking {
        try {
            val db = com.unsmah.pathpulse.data.PathPulseDatabase.getInstance(context)
            val repo = com.unsmah.pathpulse.data.repository.TrackRepository(
                db.trackDao(),
                db.trackPointDao(),
                db.waypointDao(),
                db.geofenceDao()
            )
            repo.deleteTrackById(trackId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun exportAsGPX(context: android.content.Context, track: Track, points: List<TrackPoint>) {
    runBlocking {
        try {
            val dir = File(context.getExternalFilesDir(null), "exports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${track.name.replace(" ", "_")}.gpx")
            GPXExporter.export(track, points, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun exportAsGeoJSON(context: android.content.Context, track: Track, points: List<TrackPoint>) {
    runBlocking {
        try {
            val dir = File(context.getExternalFilesDir(null), "exports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${track.name.replace(" ", "_")}.geojson")
            GeoJSONExporter.export(track, points, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun exportAsCSV(context: android.content.Context, track: Track, points: List<TrackPoint>) {
    runBlocking {
        try {
            val dir = File(context.getExternalFilesDir(null), "exports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${track.name.replace(" ", "_")}.csv")
            CSVExporter.export(track, points, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun exportAsKML(context: android.content.Context, track: Track, points: List<TrackPoint>) {
    runBlocking {
        try {
            val dir = File(context.getExternalFilesDir(null), "exports")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${track.name.replace(" ", "_")}.kml")
            KMLEporter.export(track, points, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
