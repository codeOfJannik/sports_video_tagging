package de.js329.sportsvideotagging.database

import androidx.room.*
import de.js329.sportsvideotagging.datamodels.*

@Dao
interface MatchDao {

    @Query("SELECT * FROM `Match`")
    fun getAll(): List<Match>

    @Insert
    fun insertAll(vararg matches: Match): List<Long>

    @Update
    fun updateAll(vararg matches: Match)

    @Delete
    fun delete(match: Match)
}

@Dao
interface PlayerDao {

    @Query("SELECT * FROM Player")
    fun getAll(): List<Player>

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

    @Query("SELECT * FROM Attribute")
    fun getAllAttributes(): List<EventAttribute>

    @Query("SELECT * FROM EventType")
    fun getAllEventTypes(): List<EventType>

    @Query("SELECT * FROM MatchEvent")
    fun getAllMatchEvents(): List<MatchEvent>

    @Insert
    fun insertAllEventAttributes(vararg attributes: EventAttribute): List<Long>

    @Insert
    fun insertAllEventTypes(vararg eventTypes: EventType): List<Long>

    @Insert
    fun insertAllMatchEvents(vararg matchEvents: MatchEvent): List<Long>
}

@Dao
interface EventJoinDao {

    @Transaction
    @Query("SELECT * FROM Player")
    fun getPlayersForMatchEvents(): List<PlayersForMatchEvents>

    @Transaction
    @Query("SELECT * FROM MatchEvent")
    fun getMatchEventsWithPlayers(): List<MatchEventsWithPlayers>

    @Transaction
    @Query("SELECT * FROM Attribute")
    fun getAttributesForMatchEvent(): List<AttributesForMatchEvents>

    @Transaction
    @Query("SELECT * FROM MatchEvent")
    fun getMatchEventsWithAttributes(): List<MatchEventsWithAttributes>

    @Insert
    fun insertAllEventPlayerJoins(vararg eventPlayerJoin: MatchEventPlayer): List<Long>

    @Insert
    fun insertAllEventAttributeJoins(vararg eventAttributeJoin: MatchEventAttribute): List<Long>
}