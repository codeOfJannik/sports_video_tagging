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
        MatchEventPlayer::class,
        LongTimedEventType::class,
        MatchLongTimedEvent::class
    ],
    version = 5,
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

        private val MIGRATION_2_3 = object : Migration(2,3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS EventType_New (
                    `uid` INTEGER PRIMARY KEY AUTOINCREMENT,
                     `event_title` TEXT NOT NULL,
                       `time_offset` INTEGER NOT NULL,
                        `player_selection` INTEGER NOT NULL,
                         `attributes_allowed` INTEGER NOT NULL,
                          `active_type` INTEGER NOT NULL)"""
                    .trimMargin())
                database.execSQL("INSERT INTO EventType_New SELECT uid, event_title, time_offset, player_selection, attributes_allowed, active_type FROM EventType")
                database.execSQL("DROP TABLE EventType")
                database.execSQL("ALTER TABLE EventType_New RENAME TO EventType")
                database.execSQL("CREATE TABLE IF NOT EXISTS LongTimedEventType ('uid' INTEGER PRIMARY KEY AUTOINCREMENT, 'switchable' INTEGER NOT NULL, 'eventA_title' TEXT NOT NULL, 'eventB_title' TEXT)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3,4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE LongTimedEventType ADD COLUMN active_type INTEGER NOT NULL DEFAULT 1")
                database.execSQL("""CREATE TABLE IF NOT EXISTS MatchEvent_New (
                    `matchEventId` INTEGER PRIMARY KEY AUTOINCREMENT,
                    `match` INTEGER NOT NULL,
                    `match_order_number` INTEGER NOT NULL,
                    `timestamp` INTEGER NOT NULL,
                    `event_type` INTEGER NOT NULL,
                    `following_event` INTEGER,
                    FOREIGN KEY(`match`) REFERENCES `Match`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE ,
                    FOREIGN KEY(`following_event`) REFERENCES `MatchEvent`(`matchEventId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`event_type`) REFERENCES `EventType`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE)"""
                        .trimIndent())
                database.execSQL("INSERT INTO MatchEvent_New SELECT * FROM MatchEvent")
                database.execSQL("DROP TABLE MatchEvent")
                database.execSQL("ALTER TABLE MatchEvent_New RENAME TO MatchEvent")
                database.execSQL("""CREATE TABLE IF NOT EXISTS MatchLongTimedEvent (
                    'matchLongTimedEventId' INTEGER PRIMARY KEY AUTOINCREMENT,
                     'match' INTEGER NOT NULL,
                      'match_order_number' INTEGER NOT NULL,
                       'timestamp' INTEGER NOT NULL,
                        'event_type' INTEGER NOT NULL,
                         FOREIGN KEY('match') REFERENCES 'Match'('uid') ON UPDATE NO ACTION ON DELETE CASCADE,
                          FOREIGN KEY('event_type') REFERENCES 'LongTimedEventType'('uid') ON UPDATE NO ACTION ON DELETE CASCADE)"""
                        .trimMargin())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4,5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE MatchLongTimedEvent ADD COLUMN switched_to_event_b INTEGER NOT NULL DEFAULT 0")
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
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