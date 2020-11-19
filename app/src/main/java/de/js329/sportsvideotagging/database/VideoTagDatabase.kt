package de.js329.sportsvideotagging.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.js329.sportsvideotagging.datamodels.*

@Database(entities = [Match::class, Team::class, Player::class, EventAttribute::class, EventType::class, MatchEvent::class, MatchEventAttribute::class, MatchEventPlayer::class], version = 1)
abstract class VideoTagDatabase: RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun teamDao(): TeamDao
    abstract fun matchDao(): MatchDao
    abstract fun eventDao(): EventDao
    abstract fun eventJoinDao(): EventJoinDao
}