package com.unsmah.pathpulse.location

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unsmah.pathpulse.data.PathPulseDatabase
import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import com.unsmah.pathpulse.data.repository.TrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TrackingState(
    val isTracking: Boolean = false,
    val currentTrackId: Long? = null,
    val currentTrack: Track? = null,
    val lastPoint: TrackPoint? = null,
    val points: List<TrackPoint> = emptyList(),
    val error: String? = null
)

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TrackRepository by lazy {
        val db = PathPulseDatabase.getInstance(application)
        TrackRepository(
            db.trackDao(),
            db.trackPointDao(),
            db.waypointDao(),
            db.geofenceDao()
        )
    }

    private val _trackingState = MutableStateFlow(TrackingState())
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private var serviceBound = false
    private var trackingService: TrackingService? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            serviceBound = true
            // Update state when service connects
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            trackingService = null
        }
    }

    fun startTracking(context: Context, trackId: Long) {
        viewModelScope.launch {
            try {
                val intent = Intent(context, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_START
                    putExtra(TrackingService.EXTRA_TRACK_ID, trackId)
                }
                context.startForegroundService(intent)
                _trackingState.value = _trackingState.value.copy(
                    isTracking = true,
                    currentTrackId = trackId
                )
            } catch (e: Exception) {
                _trackingState.value = _trackingState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun stopTracking(context: Context) {
        viewModelScope.launch {
            try {
                val intent = Intent(context, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_STOP
                }
                context.startService(intent)
                _trackingState.value = _trackingState.value.copy(
                    isTracking = false,
                    currentTrackId = null
                )
            } catch (e: Exception) {
                _trackingState.value = _trackingState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun pauseTracking(context: Context) {
        viewModelScope.launch {
            try {
                val intent = Intent(context, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_PAUSE
                }
                context.startService(intent)
                _trackingState.value = _trackingState.value.copy(
                    isTracking = false
                )
            } catch (e: Exception) {
                _trackingState.value = _trackingState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun resumeTracking(context: Context) {
        viewModelScope.launch {
            try {
                val intent = Intent(context, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_RESUME
                }
                context.startService(intent)
                _trackingState.value = _trackingState.value.copy(
                    isTracking = true
                )
            } catch (e: Exception) {
                _trackingState.value = _trackingState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun bindToService(context: Context) {
        val intent = Intent(context, TrackingService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun unbindFromService(context: Context) {
        if (serviceBound) {
            context.unbindService(serviceConnection)
            serviceBound = false
        }
    }

    suspend fun getActiveTrack() {
        viewModelScope.launch {
            repository.activeTrack.collect { track ->
                _trackingState.value = _trackingState.value.copy(currentTrack = track)
            }
        }
    }

    fun clearError() {
        _trackingState.value = _trackingState.value.copy(error = null)
    }
}
