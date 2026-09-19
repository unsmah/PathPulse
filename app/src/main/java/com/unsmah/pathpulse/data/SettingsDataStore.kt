package com.unsmah.pathpulse.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    companion object {
        private val KEY_MAP_ENGINE = stringPreferencesKey("map_engine")
        private val KEY_TILE_LAYER = stringPreferencesKey("tile_layer")
        private val KEY_RECORD_PROFILE = stringPreferencesKey("record_profile")
        private val KEY_INTERVAL_MS = stringPreferencesKey("interval_ms")
        private val KEY_MIN_UPDATE_METERS = stringPreferencesKey("min_update_meters")
        private val KEY_ACCURACY_FILTER_M = stringPreferencesKey("accuracy_filter_m")
        private val KEY_AUTO_PAUSE = booleanPreferencesKey("auto_pause")
        private val KEY_UNITS_METRIC = booleanPreferencesKey("units_metric")
        private val KEY_DEFAULT_EXPORT_FORMAT = stringPreferencesKey("default_export_format")
        private val KEY_TRACKING_NOTIFICATION_ENABLED = booleanPreferencesKey("tracking_notification_enabled")
    }

    val mapEngine: Flow<String> = context.dataStore.data.map { it[KEY_MAP_ENGINE] ?: "maplibre" }
    val tileLayer: Flow<String> = context.dataStore.data.map { it[KEY_TILE_LAYER] ?: "osm" }
    val recordProfile: Flow<String> = context.dataStore.data.map { it[KEY_RECORD_PROFILE] ?: "balanced" }
    val intervalMs: Flow<String> = context.dataStore.data.map { it[KEY_INTERVAL_MS] ?: "15000" }
    val minUpdateMeters: Flow<String> = context.dataStore.data.map { it[KEY_MIN_UPDATE_METERS] ?: "0" }
    val accuracyFilterM: Flow<String> = context.dataStore.data.map { it[KEY_ACCURACY_FILTER_M] ?: "50" }
    val autoPause: Flow<Boolean> = context.dataStore.data.map { it[KEY_AUTO_PAUSE] ?: true }
    val unitsMetric: Flow<Boolean> = context.dataStore.data.map { it[KEY_UNITS_METRIC] ?: true }
    val defaultExportFormat: Flow<String> = context.dataStore.data.map { it[KEY_DEFAULT_EXPORT_FORMAT] ?: "gpx" }
    val trackingNotificationEnabled: Flow<Boolean> = context.dataStore.data.map { it[KEY_TRACKING_NOTIFICATION_ENABLED] ?: true }

    suspend fun setMapEngine(engine: String) {
        context.dataStore.edit { it[KEY_MAP_ENGINE] = engine }
    }

    suspend fun setTileLayer(layer: String) {
        context.dataStore.edit { it[KEY_TILE_LAYER] = layer }
    }

    suspend fun setRecordProfile(profile: String) {
        context.dataStore.edit { it[KEY_RECORD_PROFILE] = profile }
    }

    suspend fun setIntervalMs(ms: String) {
        context.dataStore.edit { it[KEY_INTERVAL_MS] = ms }
    }

    suspend fun setMinUpdateMeters(meters: String) {
        context.dataStore.edit { it[KEY_MIN_UPDATE_METERS] = meters }
    }

    suspend fun setAccuracyFilterM(meters: String) {
        context.dataStore.edit { it[KEY_ACCURACY_FILTER_M] = meters }
    }

    suspend fun setAutoPause(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_PAUSE] = enabled }
    }

    suspend fun setUnitsMetric(metric: Boolean) {
        context.dataStore.edit { it[KEY_UNITS_METRIC] = metric }
    }

    suspend fun setDefaultExportFormat(format: String) {
        context.dataStore.edit { it[KEY_DEFAULT_EXPORT_FORMAT] = format }
    }

    suspend fun setTrackingNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_TRACKING_NOTIFICATION_ENABLED] = enabled }
    }
}
