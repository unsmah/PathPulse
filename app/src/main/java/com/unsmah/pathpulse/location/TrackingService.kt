package com.unsmah.pathpulse.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.unsmah.pathpulse.R
import com.unsmah.pathpulse.data.PathPulseDatabase
import com.unsmah.pathpulse.data.entity.TrackPoint

class TrackingService : android.app.Service() {

    companion object {
        private const val TAG = "TrackingService"
        const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1

        const val ACTION_START = "com.unsmah.pathpulse.action.START_TRACKING"
        const val ACTION_STOP = "com.unsmah.pathpulse.action.STOP_TRACKING"
        const val ACTION_PAUSE = "com.unsmah.pathpulse.action.PAUSE_TRACKING"
        const val ACTION_RESUME = "com.unsmah.pathpulse.action.RESUME_TRACKING"
        const val EXTRA_TRACK_ID = "extra_track_id"

        private const val UPDATE_INTERVAL_HIGH = 5000L
        private const val UPDATE_INTERVAL_BALANCED = 15000L
        private const val UPDATE_INTERVAL_BATTERY = 30000L

        @Volatile
        var isTracking: Boolean = false

        @Volatile
        var currentTrackId: Long? = null
    }

    private var fusedClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_START -> startTrackingFromIntent(intent)
            ACTION_STOP -> stopTracking()
            ACTION_PAUSE -> pauseTracking()
            ACTION_RESUME -> resumeTracking()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    fun startTracking(trackId: Long) {
        currentTrackId = trackId
        isTracking = true
        startForeground(NOTIFICATION_ID, createNotification("Starting..."))
        startLocationUpdates()
        Log.d(TAG, "Tracking started, trackId=$trackId")
    }

    private fun startTrackingFromIntent(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_TRACK_ID, -1L) ?: -1L
        if (id == -1L) {
            Log.e(TAG, "No track ID provided")
            stopSelf()
            return
        }
        startTracking(id)
    }

    fun stopTracking() {
        isTracking = false
        stopLocationUpdates()
        currentTrackId = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        Log.d(TAG, "Tracking stopped")
    }

    fun pauseTracking() {
        isTracking = false
        stopLocationUpdates()
        updateNotification("Paused")
        Log.d(TAG, "Tracking paused")
    }

    fun resumeTracking() {
        isTracking = true
        startLocationUpdates()
        updateNotification("Resumed")
        Log.d(TAG, "Tracking resumed")
    }

    private fun startLocationUpdates() {
        if (fusedClient == null) return

        val interval = UPDATE_INTERVAL_BALANCED
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            interval
        ).apply {
            setMinUpdateIntervalMillis(interval / 2)
            setMinUpdateDistanceMeters(0f)
        }.build()

        try {
            fusedClient?.requestLocationUpdates(request, locationCallback!!, mainLooper)
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission missing", e)
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { fusedClient?.removeLocationUpdates(it) }
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (!isTracking || currentTrackId == null) return

                result.lastLocation?.let { location ->
                    if (location.accuracy <= 50f) {
                        val point = TrackPoint(
                            trackId = currentTrackId!!,
                            latitude = location.latitude,
                            longitude = location.longitude,
                            altitude = location.altitude,
                            accuracy = location.accuracy,
                            speed = location.speed.toDouble(),
                            bearing = location.bearing,
                            timestamp = location.time
                        )
                        kotlinx.coroutines.runBlocking {
                            val db = PathPulseDatabase.getInstance(applicationContext)
                            db.trackPointDao().insertPoint(point)
                        }
                        updateNotification(
                            "%.4f, %.4f".format(location.latitude, location.longitude)
                        )
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows current tracking status"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String): Notification {
        val intent = Intent(this, com.unsmah.pathpulse.ui.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PathPulse Tracking")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("PathPulse Tracking")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher)
            .setOngoing(true)
            .build()
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, notification)
    }
}
