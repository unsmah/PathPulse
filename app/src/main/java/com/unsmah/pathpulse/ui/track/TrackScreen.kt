package com.unsmah.pathpulse.ui.track

import androidx.compose.ui.platform.LocalContext
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.unsmah.pathpulse.ui.MainViewModel
import com.unsmah.pathpulse.ui.components.EmptyState
import com.unsmah.pathpulse.ui.components.LoadingState
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackScreen(
    viewModel: MainViewModel,
    onStartTracking: (Long) -> Unit,
    onStopTracking: () -> Unit,
    onPauseTracking: () -> Unit,
    onResumeTracking: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTrack by remember { mutableStateOf<Track?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isTracking by remember { mutableStateOf(false) }
    var currentPoint by remember { mutableStateOf<TrackPoint?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (activeTrack != null) {
                        Text(
                            text = activeTrack!!.name,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.track_start_new),
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.track_view_history)
                        )
                    }
                    IconButton(onClick = onNavigateToMap) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = stringResource(R.string.track_view_map)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.track_distance),
                    value = formatDistance(activeTrack?.distanceMeters ?: 0.0),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.track_duration),
                    value = formatDuration(activeTrack?.durationMillis ?: 0L),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.track_current_speed),
                    value = formatSpeed(currentPoint?.speed ?: 0.0),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.track_avg_speed),
                    value = formatSpeed(activeTrack?.avgSpeedMps ?: 0.0),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            StatCard(
                label = stringResource(R.string.track_points),
                value = activeTrack?.pointCount?.toString() ?: "0",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            FloatingActionButton(
                onClick = {
                    if (isTracking) {
                        onStopTracking()
                    } else {
                        startNewTrack(context, onStartTracking)
                    }
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
                containerColor = if (isTracking) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = if (isTracking) {
                        Icons.Default.Stop
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (isTracking) {
                        stringResource(R.string.track_stop)
                    } else {
                        stringResource(R.string.track_start)
                    },
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.map_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    if (isLoading) {
        LoadingState()
    }

    if (!isLoading && activeTrack == null && !isTracking) {
        EmptyState(
            icon = Icons.Default.PlayArrow,
            title = stringResource(R.string.track_start_new),
            message = stringResource(R.string.history_empty_message)
        )
    }

    loadActiveTrack(context) { track ->
        activeTrack = track
        isLoading = false
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
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
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatSpeed(mps: Double): String {
    val kmh = mps * 3.6
    return String.format("%.1f km/h", kmh)
}

private fun startNewTrack(
    context: Context,
    onStartTracking: (Long) -> Unit
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
            val trackName = "Track ${java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            val trackId = repo.startTrack(trackName)
            onStartTracking(trackId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


private fun loadActiveTrack(context: Context, onResult: (Track?) -> Unit) {
    runBlocking {
        try {
            val db = com.unsmah.pathpulse.data.PathPulseDatabase.getInstance(context)
            val repo = com.unsmah.pathpulse.data.repository.TrackRepository(
                db.trackDao(),
                db.trackPointDao(),
                db.waypointDao(),
                db.geofenceDao()
            )
            onResult(repo.getLatestTrack())
        } catch (e: Exception) {
            onResult(null)
        }
    }
}
