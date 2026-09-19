package com.unsmah.pathpulse.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Resume tracking if it was active before reboot
            // In a full implementation, check preferences/DB for active track
            // and restart the service if needed
            val serviceIntent = Intent(context, TrackingService::class.java).apply {
                action = TrackingService.ACTION_RESUME
            }
            context.startForegroundService(serviceIntent)
        }
    }
}
