package com.unsmah.pathpulse

import android.app.Application
import com.unsmah.pathpulse.data.PathPulseDatabase
import com.unsmah.pathpulse.data.SettingsDataStore

class PathPulseApp : Application() {

    lateinit var database: PathPulseDatabase
        private set

    lateinit var settingsDataStore: SettingsDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        database = PathPulseDatabase.getInstance(this)
        settingsDataStore = SettingsDataStore(this)
    }
}
