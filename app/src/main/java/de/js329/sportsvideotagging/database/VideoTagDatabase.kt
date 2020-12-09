package de.js329.sportsvideotagging.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import java.util.concurrent.Executors
import androidx.sqlite.db.SupportSQLiteDatabase
import de.js329.sportsvideotagging.datamodels.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible

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
    exportSchema = true
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
        fun getInstance(context: Context, scope: CoroutineScope): VideoTagDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                        context,
                        VideoTagDatabase::class.java,
                        "videoTaggingDB"
                )
                    .createFromAsset("database/db_v1.json")
                    .addCallback(VideoTagDatabaseCallback(scope))
                    .build()
            }
            return INSTANCE as VideoTagDatabase
        }
    }

    private class VideoTagDatabaseCallback(
        private val scope: CoroutineScope
    ): RoomDatabase.Callback() {
        private val prePopulateStartMatchEventType = EventType(
            null,
            "Match Start",
            longTimedEvent = false,
            0,
            playerSelection = false,
            attributesAllowed = false,
            activeEventType = false
        )

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {
                scope.launch { it.eventDao().insert(prePopulateStartMatchEventType) }
            }
        }
    }
}