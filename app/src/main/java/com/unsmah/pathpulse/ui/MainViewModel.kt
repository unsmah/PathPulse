package com.unsmah.pathpulse.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unsmah.pathpulse.data.PathPulseDatabase
import com.unsmah.pathpulse.data.SettingsDataStore
import com.unsmah.pathpulse.location.TrackingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class MapEngine { MAPLIBRE, LEAFLET }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = PathPulseDatabase.getInstance(application)
    private val settingsDataStore = SettingsDataStore(application)

    private val _mapEngine = MutableStateFlow(MapEngine.MAPLIBRE)
    val mapEngine: StateFlow<MapEngine> = _mapEngine.asStateFlow()
    private val _tileLayer = MutableStateFlow("osm")
    val tileLayer: StateFlow<String> = _tileLayer.asStateFlow()

    init {
        viewModelScope.launch {
            settingsDataStore.mapEngine.collect { engine ->
                _mapEngine.value = when (engine) {
                    "maplibre" -> MapEngine.MAPLIBRE
                    else -> MapEngine.LEAFLET
                }
            }
        }
        viewModelScope.launch {
            settingsDataStore.tileLayer.collect { layer ->
                _tileLayer.value = layer
            }
        }
    }

    fun setMapEngine(engine: MapEngine) {
        viewModelScope.launch {
            settingsDataStore.setMapEngine(if (engine == MapEngine.MAPLIBRE) "maplibre" else "leaflet")
        }
    }

    fun setTileLayer(layer: String) {
        viewModelScope.launch {
            settingsDataStore.setTileLayer(layer)
        }
    }

    fun startTracking(context: android.content.Context, trackId: Long) {
        val intent = android.content.Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_START
            putExtra(TrackingService.EXTRA_TRACK_ID, trackId)
        }
        context.startForegroundService(intent)
    }

    fun stopTracking(context: android.content.Context) {
        val intent = android.content.Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_STOP
        }
        context.startService(intent)
    }

    fun pauseTracking(context: android.content.Context) {
        val intent = android.content.Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_PAUSE
        }
        context.startService(intent)
    }

    fun resumeTracking(context: android.content.Context) {
        val intent = android.content.Intent(context, TrackingService::class.java).apply {
            action = TrackingService.ACTION_RESUME
        }
        context.startService(intent)
    }
}
