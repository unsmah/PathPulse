package com.unsmah.pathpulse.ui.map

import com.unsmah.pathpulse.ui.MapEngine
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.unsmah.pathpulse.ui.MainViewModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onNavigateToLiveMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTrack by remember { mutableStateOf<Track?>(null) }
    var selectedEngine by remember { mutableStateOf("maplibre") }
    var selectedLayer by remember { mutableStateOf("osm") }
    var showEngineMenu by remember { mutableStateOf(false) }
    var showLayerMenu by remember { mutableStateOf(false) }
    var trafficOverlay by remember { mutableStateOf(false) }
    var terrainOverlay by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                actions = {
                    // Engine selector
                    ExposedDropdownMenuBox(
                        expanded = showEngineMenu,
                        onExpandedChange = { showEngineMenu = it },
                    ) {
                        val engines = listOf(
                            "maplibre" to stringResource(R.string.map_engine_maplibre),
                            "leaflet" to stringResource(R.string.map_engine_leaflet)
                        )
                        val currentEngine = engines.find { it.first == selectedEngine }?.second
                            ?: stringResource(R.string.map_engine_maplibre)
                        
                        Text(
                            text = currentEngine,
                            modifier = Modifier
                                .menuAnchor()
                                .clickable { showEngineMenu = true }
                        )
                        ExposedDropdownMenu(
                            expanded = showEngineMenu,
                            onDismissRequest = { showEngineMenu = false }
                        ) {
                            engines.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedEngine = value
                                        viewModel.setMapEngine(
                                            if (value == "maplibre") {
                                                MapEngine.MAPLIBRE
                                            } else {
                                                MapEngine.LEAFLET
                                            }
                                        )
                                        showEngineMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Layer selector
                    ExposedDropdownMenuBox(
                        expanded = showLayerMenu,
                        onExpandedChange = { showLayerMenu = it },
                    ) {
                        val layers = listOf(
                            "osm" to stringResource(R.string.map_layer_osm),
                            "satellite" to stringResource(R.string.map_layer_satellite),
                            "topo" to stringResource(R.string.map_layer_topo)
                        )
                        val currentLayer = layers.find { it.first == selectedLayer }?.second
                            ?: stringResource(R.string.map_layer_osm)
                        
                        Text(
                            text = currentLayer,
                            modifier = Modifier
                                .menuAnchor()
                                .clickable { showLayerMenu = true }
                        )
                        ExposedDropdownMenu(
                            expanded = showLayerMenu,
                            onDismissRequest = { showLayerMenu = false }
                        ) {
                            layers.forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedLayer = value
                                        viewModel.setTileLayer(value)
                                        showLayerMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onNavigateToLiveMap) {
                        Icon(
                            imageVector = Icons.Default.TrackChanges,
                            contentDescription = stringResource(R.string.map_live_tracking)
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
        ) {
            // Map WebView area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Map View",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Controls panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.map_overlays),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.map_overlays_traffic),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Switch(
                        checked = trafficOverlay,
                        onCheckedChange = { trafficOverlay = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.map_overlays_terrain),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.material3.Switch(
                        checked = terrainOverlay,
                        onCheckedChange = { terrainOverlay = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Location button
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.map_center_location),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Center on your current location",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(
                            onClick = { /* Center on location */ },
                            colors = IconButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusStrong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    loadActiveTrack(context) { track ->
        activeTrack = track
    }
}


private fun loadActiveTrack(context: android.content.Context, onResult: (Track?) -> Unit) {
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

private object IconButtonDefaults {
    fun colors(containerColor: androidx.compose.ui.graphics.Color) = 
        object : androidx.compose.material3.IconButtonDefaults.IconButtonColors {
            override val containerColor: androidx.compose.ui.graphics.Color get() = containerColor
        }
}
