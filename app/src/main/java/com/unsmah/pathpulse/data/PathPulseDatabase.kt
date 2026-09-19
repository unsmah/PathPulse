package com.unsmah.pathpulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.unsmah.pathpulse.data.dao.GeofenceDao
import com.unsmah.pathpulse.data.dao.TrackDao
import com.unsmah.pathpulse.data.dao.TrackPointDao
import com.unsmah.pathpulse.data.dao.WaypointDao
import com.unsmah.pathpulse.data.entity.Geofence
import com.unsmah.pathpulse.data.entity.GeofenceEvent
import com.unsmah.pathpulse.data.entity.Track
import com.unsmah.pathpulse.data.entity.TrackPoint
import com.unsmah.pathpulse.data.entity.Waypoint

@Database(
    entities = [
        Track::class,
        TrackPoint::class,
        Waypoint::class,
        Geofence::class,
        GeofenceEvent::class
    ],
    version = 1,
    exportSchema = true
)
abstract class PathPulseDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun trackPointDao(): TrackPointDao
    abstract fun waypointDao(): WaypointDao
    abstract fun geofenceDao(): GeofenceDao

    companion object {
        @Volatile
        private var INSTANCE: PathPulseDatabase? = null

        fun getInstance(context: Context): PathPulseDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PathPulseDatabase::class.java,
                    "pathpulse.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
