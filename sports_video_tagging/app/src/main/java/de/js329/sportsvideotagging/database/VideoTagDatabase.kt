package de.js329.sportsvideotagging.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.js329.sportsvideotagging.datamodels.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception

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
    version = 7,
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
                    `match_sequence_number` INTEGER NOT NULL,
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
                    `match_sequence_number` INTEGER NOT NULL,
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
                      'match_sequence_number' INTEGER NOT NULL,
                       'timestamp' INTEGER NOT NULL,
                        'event_type' INTEGER NOT NULL,
                         FOREIGN KEY('match') REFERENCES 'Match'('uid') ON UPDATE NO ACTION ON DELETE CASCADE,
                          FOREIGN KEY('event_type') REFERENCES 'LongTimedEventType'('uid') ON UPDATE NO ACTION ON DELETE CASCADE)"""
                    .trimIndent())
            }
        }

        private val MIGRATION_4_5 = object : Migration(4,5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE MatchLongTimedEvent ADD COLUMN switched_to_event_b INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5,6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    val c = database.query("SELECT * FROM Match")
                    c.use {
                        if (c.moveToFirst()) {
                            val cv = ContentValues()
                            cv.put("id", c.getLong((c.getColumnIndex("uid"))))
                            cv.put("date", c.getLong((c.getColumnIndex("date"))))
                            cv.put("home_team", c.getLong((c.getColumnIndex("home_team"))))
                            cv.put("guest_team", c.getLong((c.getColumnIndex("away_team"))))
                            cv.put("home_score", c.getLong((c.getColumnIndex("home_score"))))
                            cv.put("guest_score", c.getLong((c.getColumnIndex("away_score"))))
                            database.execSQL("DROP TABLE IF EXISTS 'Match'")
                            createMatchTable(database)
                            database.insert("Match", 0, cv)
                        } else {
                            database.execSQL("DROP TABLE IF EXISTS 'Match'")
                            createMatchTable(database)
                        }
                    }
                } catch (e: SQLiteException) {
                    Log.e(e.localizedMessage, "SQLLiteException in Migration from database version 5 to 6")
                } catch (e: Exception) {
                    Log.e(e.localizedMessage, "Failed to migrate database version 5 to version 6")
                }
            }
        }

        private val MIGRATION_6_7 = object : Migration(6,7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    val matchEventCursor = database.query("SELECT * FROM MatchEvent")
                    val longMatchEventCursor = database.query("SELECT * FROM MatchLongTimedEvent")
                    matchEventCursor.use {
                        if (matchEventCursor.moveToFirst()) {
                            val cv = ContentValues()
                            cv.put("matchEventId", matchEventCursor.getLong(matchEventCursor.getColumnIndex("matchEventId")))
                            cv.put("match", matchEventCursor.getLong(matchEventCursor.getColumnIndex("match")))
                            cv.put("match_sequence_number", matchEventCursor.getLong(matchEventCursor.getColumnIndex("match_order_number")))
                            cv.put("timestamp", matchEventCursor.getLong(matchEventCursor.getColumnIndex("timestamp")))
                            cv.put("event_type", matchEventCursor.getLong(matchEventCursor.getColumnIndex("event_type")))
                            cv.put("following_event", matchEventCursor.getLong(matchEventCursor.getColumnIndex("following_event")))
                            database.execSQL("DROP TABLE IF EXISTS 'MatchEvent'")
                            createMatchEventTable(database)
                            database.insert("MatchEvent", 0, cv)
                        } else {
                            database.execSQL("DROP TABLE IF EXISTS 'MatchEvent'")
                            createMatchEventTable(database)
                        }
                    }
                    longMatchEventCursor.use {
                        if (longMatchEventCursor.moveToFirst()) {
                            val cv = ContentValues()
                            cv.put("matchLongTimedEventId", longMatchEventCursor.getLong((longMatchEventCursor.getColumnIndex("matchLongTimedEventId"))))
                            cv.put("match", longMatchEventCursor.getLong((longMatchEventCursor.getColumnIndex("match"))))
                            cv.put("match_sequence_number", longMatchEventCursor.getLong((longMatchEventCursor.getColumnIndex("match_order_number"))))
                            cv.put("timestamp", longMatchEventCursor.getLong((longMatchEventCursor.getColumnIndex("timestamp"))))
                            cv.put("event_type", longMatchEventCursor.getLong((longMatchEventCursor.getColumnIndex("event_type"))))
                            cv.put("switched_to_event_b", longMatchEventCursor.getLong((longMatchEventCursor.getColumnIndex("switched_to_event_b"))))
                            database.execSQL("DROP TABLE IF EXISTS 'MatchLongTimedEvent'")
                            createMatchLongTimedEventTable(database)
                            database.insert("MatchLongTimedEvent", 0, cv)
                        } else {
                            database.execSQL("DROP TABLE IF EXISTS 'MatchLongTimedEvent'")
                            createMatchLongTimedEventTable(database)
                        }
                    }
                } catch (e: SQLiteException) {
                    Log.e(e.localizedMessage, "SQLLiteException in Migration from database version 6 to 7")
                } catch (e: Exception) {
                    Log.e(e.localizedMessage, "Failed to migrate database version 6 to version 7")
                }
            }
        }

        private fun createMatchTable(database: SupportSQLiteDatabase) {
            database.execSQL("""CREATE TABLE IF NOT EXISTS `Match` (
            `uid` INTEGER PRIMARY KEY AUTOINCREMENT,
             `date` INTEGER,
              `home_team` INTEGER NOT NULL,
               `guest_team` INTEGER NOT NULL,
                `home_score` INTEGER NOT NULL,
                 `guest_score` INTEGER NOT NULL,
                  FOREIGN KEY(`home_team`) REFERENCES `Team`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE,
                   FOREIGN KEY(`guest_team`) REFERENCES `Team`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )""".trimIndent())
        }

        private fun createMatchEventTable(database: SupportSQLiteDatabase) {
            database.execSQL("""CREATE TABLE IF NOT EXISTS `MatchEvent` (
            `matchEventId` INTEGER PRIMARY KEY AUTOINCREMENT,
             `match` INTEGER NOT NULL,
              `match_sequence_number` INTEGER NOT NULL,
                `timestamp` INTEGER NOT NULL,
                `event_type` INTEGER NOT NULL,
                `following_event` INTEGER,
                FOREIGN KEY(`match`) REFERENCES `Match`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`following_event`) REFERENCES `MatchEvent`(`matchEventId`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`event_type`) REFERENCES `EventType`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )""".trimIndent())
        }

        private fun createMatchLongTimedEventTable(database: SupportSQLiteDatabase) {
            database.execSQL("""CREATE TABLE IF NOT EXISTS `MatchLongTimedEvent` (
                `matchLongTimedEventId` INTEGER PRIMARY KEY AUTOINCREMENT,
                 `match` INTEGER NOT NULL,
                  `match_sequence_number` INTEGER NOT NULL,
                   `timestamp` INTEGER NOT NULL,
                    `event_type` INTEGER NOT NULL,
                     `switched_to_event_b` INTEGER NOT NULL,
                      FOREIGN KEY(`match`) REFERENCES `Match`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE,
                       FOREIGN KEY(`event_type`) REFERENCES `LongTimedEventType`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )""".trimIndent())
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
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
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