package de.js329.sportsvideotagging.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.js329.sportsvideotagging.datamodels.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    version = 2,
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

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS MatchEvent_New (
                    `matchEventId` INTEGER PRIMARY KEY AUTOINCREMENT,
                    `match` INTEGER NOT NULL,
                    `match_order_number` INTEGER NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `event_type` INTEGER NOT NULL,
                    `following_event` INTEGER,
                    FOREIGN KEY(`match`) REFERENCES `Match`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE ,
                    FOREIGN KEY(`following_event`) REFERENCES `MatchEvent`(`matchEventId`) ON UPDATE NO ACTION ON DELETE CASCADE )"""
                    .trimIndent())
                database.execSQL("INSERT INTO MatchEvent_New SELECT * FROM MatchEvent")
                database.execSQL("DROP TABLE MatchEvent")
                database.execSQL("ALTER TABLE MatchEvent_New RENAME TO MatchEvent")

            }
        }

        fun getInstance(context: Context, scope: CoroutineScope): VideoTagDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(
                    context,
                    VideoTagDatabase::class.java,
                    "videoTaggingDB"
                )
                    .createFromAsset("database/db_v1.json")
                    .addCallback(VideoTagDatabaseCallback(scope))
                    .addMigrations(MIGRATION_1_2)
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