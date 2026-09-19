package com.unsmah.pathpulse.ui.history

import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.unsmah.pathpulse.R
import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.ui.MainViewModel
import com.unsmah.pathpulse.ui.components.EmptyState
import com.unsmah.pathpulse.ui.components.LoadingState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateToTrackDetail: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var sortBy by remember { mutableStateOf(SortBy.DATE_DESC) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.history_title)) },
                actions = {
                    IconButton(onClick = { sortBy = sortBy.next() }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = stringResource(R.string.history_sort)
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
        } else if (tracks.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.history_empty),
                message = stringResource(R.string.history_empty_message)
            )
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
            ) {
                items(tracks, key = { it.id }) { track ->
                    HistoryTrackCard(
                        track = track,
                        onClick = { onNavigateToTrackDetail(track.id) },
                        onDelete = { showDeleteDialog = track.id }
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { trackId ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.history_delete_confirm)) },
            text = { Text(stringResource(R.string.history_delete_message)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        deleteTrack(context, trackId)
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    loadTracks(context) { loadedTracks ->
        tracks = when (sortBy) {
            SortBy.DATE_DESC -> loadedTracks.sortedByDescending { it.startTime }
            SortBy.DATE_ASC -> loadedTracks.sortedBy { it.startTime }
            SortBy.DISTANCE_DESC -> loadedTracks.sortedByDescending { it.distanceMeters }
            SortBy.DISTANCE_ASC -> loadedTracks.sortedBy { it.distanceMeters }
            SortBy.DURATION_DESC -> loadedTracks.sortedByDescending { it.durationMillis }
            SortBy.DURATION_ASC -> loadedTracks.sortedBy { it.durationMillis }
        }
        isLoading = false
    }
}

@Composable
private fun HistoryTrackCard(
    track: Track,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(
                        R.string.history_sort_date,
                        dateFormat.format(Date(track.startTime))
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = formatDistance(track.distanceMeters),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatDuration(track.durationMillis),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${track.pointCount} pts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.history_delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
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
    return if (hours > 0) {
        String.format("%dh %dm", hours, minutes)
    } else {
        String.format("%dm %ds", minutes, totalSeconds % 60)
    }
}


private fun loadTracks(context: android.content.Context, onResult: (List<Track>) -> Unit) {
    runBlocking {
        try {
            val db = com.unsmah.pathpulse.data.PathPulseDatabase.getInstance(context)
            val repo = com.unsmah.pathpulse.data.repository.TrackRepository(
                db.trackDao(),
                db.trackPointDao(),
                db.waypointDao(),
                db.geofenceDao()
            )
            onResult(repo.allTracks.first())
        } catch (e: Exception) {
            onResult(emptyList())
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

private enum class SortBy {
    DATE_DESC, DATE_ASC, DISTANCE_DESC, DISTANCE_ASC, DURATION_DESC, DURATION_ASC;

    fun next(): SortBy = when (this) {
        DATE_DESC -> DATE_ASC
        DATE_ASC -> DISTANCE_DESC
        DISTANCE_DESC -> DISTANCE_ASC
        DISTANCE_ASC -> DURATION_DESC
        DURATION_DESC -> DURATION_ASC
        DURATION_ASC -> DATE_DESC
    }
}
