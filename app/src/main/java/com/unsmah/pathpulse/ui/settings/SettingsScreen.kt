package com.unsmah.pathpulse.ui.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.unsmah.pathpulse.R
import com.unsmah.pathpulse.ui.MainViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDeleteAllDialog by remember { mutableStateOf(false) }

    // Map settings
    var selectedEngine by remember { mutableStateOf("maplibre") }
    var showEngineMenu by remember { mutableStateOf(false) }
    var selectedLayer by remember { mutableStateOf("osm") }
    var showLayerMenu by remember { mutableStateOf(false) }
    var trafficOverlay by remember { mutableStateOf(false) }
    var terrainOverlay by remember { mutableStateOf(false) }

    // Recording settings
    var recordingProfile by remember { mutableStateOf("walking") }
    var showProfileMenu by remember { mutableStateOf(false) }
    var recordingInterval by remember { mutableStateOf(5.0) }
    var minDistance by remember { mutableStateOf(5.0) }
    var accuracyFilter by remember { mutableStateOf(20.0) }
    var autoPause by remember { mutableStateOf(true) }

    // Export settings
    var defaultFormat by remember { mutableStateOf("gpx") }
    var showFormatMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            // Map settings section
            item {
                SettingsSection(title = stringResource(R.string.settings_map)) {
                    // Engine selector
                    SettingsDropdownItem(
                        icon = Icons.Default.Map,
                        label = stringResource(R.string.settings_map_engine),
                        value = when (selectedEngine) {
                            "maplibre" -> stringResource(R.string.map_engine_maplibre)
                            else -> stringResource(R.string.map_engine_leaflet)
                        },
                        expanded = showEngineMenu,
                        onExpandedChange = { showEngineMenu = it },
                        menuItems = listOf(
                            "maplibre" to stringResource(R.string.map_engine_maplibre),
                            "leaflet" to stringResource(R.string.map_engine_leaflet)
                        ),
                        onItemSelected = { value ->
                            selectedEngine = value
                            viewModel.setMapEngine(
                                if (value == "maplibre") {
                                    MapEngine.MAPLIBRE
                                } else {
                                    MapEngine.LEAFLET
                                }
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Layer selector
                    SettingsDropdownItem(
                        icon = Icons.Default.Map,
                        label = stringResource(R.string.settings_tile_layer),
                        value = when (selectedLayer) {
                            "osm" -> stringResource(R.string.map_layer_osm)
                            "satellite" -> stringResource(R.string.map_layer_satellite)
                            "topo" -> stringResource(R.string.map_layer_topo)
                            else -> stringResource(R.string.map_layer_osm)
                        },
                        expanded = showLayerMenu,
                        onExpandedChange = { showLayerMenu = it },
                        menuItems = listOf(
                            "osm" to stringResource(R.string.map_layer_osm),
                            "satellite" to stringResource(R.string.map_layer_satellite),
                            "topo" to stringResource(R.string.map_layer_topo)
                        ),
                        onItemSelected = { value ->
                            selectedLayer = value
                            viewModel.setTileLayer(value)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.settings_overlays),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSwitchItem(
                        label = stringResource(R.string.map_overlays_traffic),
                        checked = trafficOverlay,
                        onCheckedChange = { trafficOverlay = it }
                    )

                    SettingsSwitchItem(
                        label = stringResource(R.string.map_overlays_terrain),
                        checked = terrainOverlay,
                        onCheckedChange = { terrainOverlay = it }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Recording settings section
            item {
                SettingsSection(title = stringResource(R.string.settings_recording)) {
                    // Profile selector
                    SettingsDropdownItem(
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.settings_profile),
                        value = when (recordingProfile) {
                            "walking" -> stringResource(R.string.settings_profile_walking)
                            "cycling" -> stringResource(R.string.settings_profile_cycling)
                            "driving" -> stringResource(R.string.settings_profile_driving)
                            else -> stringResource(R.string.settings_profile_walking)
                        },
                        expanded = showProfileMenu,
                        onExpandedChange = { showProfileMenu = it },
                        menuItems = listOf(
                            "walking" to stringResource(R.string.settings_profile_walking),
                            "cycling" to stringResource(R.string.settings_profile_cycling),
                            "driving" to stringResource(R.string.settings_profile_driving)
                        ),
                        onItemSelected = { value ->
                            recordingProfile = value
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SettingsSliderItem(
                        label = stringResource(R.string.settings_interval),
                        value = recordingInterval,
                        onValueChange = { recordingInterval = it },
                        valueRange = 1.0..60.0,
                        steps = 58,
                        suffix = "s"
                    )

                    SettingsSliderItem(
                        label = stringResource(R.string.settings_min_distance),
                        value = minDistance,
                        onValueChange = { minDistance = it },
                        valueRange = 1.0..100.0,
                        steps = 98,
                        suffix = "m"
                    )

                    SettingsSliderItem(
                        label = stringResource(R.string.settings_accuracy_filter),
                        value = accuracyFilter,
                        onValueChange = { accuracyFilter = it },
                        valueRange = 5.0..100.0,
                        steps = 94,
                        suffix = "m"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsSwitchItem(
                        label = stringResource(R.string.settings_auto_pause),
                        checked = autoPause,
                        onCheckedChange = { autoPause = it }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Export settings section
            item {
                SettingsSection(title = stringResource(R.string.settings_export)) {
                    SettingsDropdownItem(
                        icon = Icons.Default.SdCard,
                        label = stringResource(R.string.settings_default_format),
                        value = when (defaultFormat) {
                            "gpx" -> "GPX"
                            "geojson" -> "GeoJSON"
                            "csv" -> "CSV"
                            "kml" -> "KML"
                            else -> "GPX"
                        },
                        expanded = showFormatMenu,
                        onExpandedChange = { showFormatMenu = it },
                        menuItems = listOf(
                            "gpx" to "GPX",
                            "geojson" to "GeoJSON",
                            "csv" to "CSV",
                            "kml" to "KML"
                        ),
                        onItemSelected = { value ->
                            defaultFormat = value
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Data management section
            item {
                SettingsSection(title = stringResource(R.string.settings_data)) {
                    SettingsClickableItem(
                        icon = Icons.Default.SdCard,
                        label = stringResource(R.string.settings_storage),
                        onClick = { /* View storage */ }
                    )

                    SettingsClickableItem(
                        icon = Icons.Default.Delete,
                        label = stringResource(R.string.settings_clear_cache),
                        onClick = { /* Clear cache */ }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsClickableItem(
                        icon = Icons.Default.Delete,
                        label = stringResource(R.string.settings_delete_all),
                        onClick = { showDeleteAllDialog = true },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            // About section
            item {
                SettingsSection(title = stringResource(R.string.settings_about)) {
                    SettingsClickableItem(
                        icon = Icons.Default.Info,
                        label = stringResource(R.string.settings_version) + ": 1.0.0",
                        onClick = { }
                    )

                    SettingsClickableItem(
                        icon = Icons.Default.Info,
                        label = stringResource(R.string.settings_licenses),
                        onClick = { /* Show licenses */ }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    // Delete all confirmation dialog
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text(stringResource(R.string.settings_delete_all_confirm)) },
            text = { Text(stringResource(R.string.settings_delete_all_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteAllData(context)
                        showDeleteAllDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsDropdownItem(
    icon: ImageVector,
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    menuItems: List<Pair<String, String>>,
    onItemSelected: (String) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .menuAnchor(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            menuItems.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onItemSelected(value)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsSliderItem(
    label: String,
    value: Double,
    onValueChange: (Double) -> Unit,
    valueRange: ClosedFloatingPointRange<Double>,
    steps: Int,
    suffix: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = String.format("%.0f$suffix", value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = tint
        )
    }
}


private fun deleteAllData(context: android.content.Context) {
    runBlocking {
        try {
            val db = com.unsmah.pathpulse.data.PathPulseDatabase.getInstance(context)
            val repo = com.unsmah.pathpulse.data.repository.TrackRepository(
                db.trackDao(),
                db.trackPointDao(),
                db.waypointDao(),
                db.geofenceDao()
            )
            repo.deleteAllData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
