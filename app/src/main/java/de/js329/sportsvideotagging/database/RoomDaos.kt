package de.js329.sportsvideotagging.database

import androidx.room.*
import de.js329.sportsvideotagging.datamodels.*

@Dao
interface MatchDao {

    @Insert
    fun insertAll(vararg matches: Match)

    @Update
    fun updateAll(vararg matches: Match)

    @Delete
    fun delete(match: Match)
}

@Dao
interface PlayerDao {
    @Insert
    fun insertAll(vararg players: Player): List<Long>

    @Update
    fun updateAll(vararg players: Player)

    @Delete
    fun delete(player: Player)
}

@Dao
interface TeamDao {

    @Query("SELECT * FROM Team")
    fun getAll(): List<Team>

    @Insert
    fun insertAll(vararg teams: Team): List<Long>

    @Update
    fun updateAll(vararg teams: Team)

    @Delete
    fun delete(team: Team)
}

@Dao
interface EventDao {

    @Insert
    fun insertAllEventAttributes(vararg attributes: EventAttribute): List<Long>

    @Insert
    fun insertAllEventTypes(vararg eventTypes: EventType): List<Long>

    @Insert
    fun insertAllMatchEvents(vararg matchEvents: MatchEvent): List<Long>
}

@Dao
interface EventJoinDao {

    @Insert
    fun insertAllEventPlayerJoins(vararg eventPlayerJoin: MatchEventPlayer): List<Long>

    @Insert
    fun insertAllEventAttributeJoins(vararg eventAttributeJoin: MatchEventAttribute): List<Long>
}