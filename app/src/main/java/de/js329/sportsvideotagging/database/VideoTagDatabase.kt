package de.js329.sportsvideotagging.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.js329.sportsvideotagging.datamodels.*

@Database(
    entities = [
        Match::class,
        Team::class,
        Player::class,
        EventAttribute::class,
        EventType::class,
        MatchEvent::class,
        MatchEventAttribute::class,
        MatchEventPlayer::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class VideoTagDatabase: RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun eventDao(): EventDao
    abstract fun eventJoinDao(): EventJoinDao

    companion object {
        private var INSTANCE: VideoTagDatabase? = null
        fun getInstance(context: Context): VideoTagDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                        context,
                        VideoTagDatabase::class.java,
                        "videoTaggingDB"
                ).build()
            }
            return INSTANCE as VideoTagDatabase
        }
    }
}