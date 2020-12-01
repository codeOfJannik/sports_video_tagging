package de.js329.sportsvideotagging.database

import androidx.room.*
import de.js329.sportsvideotagging.datamodels.*

@Dao
interface MatchDao {

    @Query("SELECT * FROM `Match`")
    suspend fun getAll(): List<Match>

    @Insert
    fun insertAll(vararg matches: Match): List<Long>

    @Insert
    fun insert(match: Match): Long

    @Update
    fun updateAll(vararg matches: Match)

    @Delete
    fun delete(match: Match)
}

@Dao
interface PlayerDao {

    @Query("SELECT * FROM Player")
    suspend fun getAll(): List<Player>

    @Query("SELECT * FROM Player WHERE team = :teamId")
    suspend fun getPlayersForTeam(teamId: Long): List<Player>

    @Insert
    fun insertAll(vararg players: Player): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player): Long

    @Update
    fun updateAll(vararg players: Player)

    @Delete
    suspend fun delete(player: Player)
}

@Dao
interface TeamDao {

    @Query("SELECT * FROM Team")
    suspend fun getAll(): List<Team>

    @Query("SELECT * FROM Team WHERE uid = :teamId")
    suspend fun getTeamForId(teamId: Long): Team?

    @Insert
    suspend fun insertAll(vararg teams: Team): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(team: Team): Long

    @Update
    fun updateAll(vararg teams: Team)

    @Delete
    suspend fun delete(team: Team)
}

@Dao
interface EventDao {

    @Query("SELECT * FROM Attribute")
    suspend fun getAllAttributes(): List<EventAttribute>

    @Query("SELECT * FROM EventType")
    suspend fun getAllEventTypes(): List<EventType>

    @Query("SELECT * FROM MatchEvent")
    suspend fun getAllMatchEvents(): List<MatchEvent>

    @Insert
    fun insertAllEventAttributes(vararg attributes: EventAttribute): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(eventAttribute: EventAttribute): Long

    @Insert
    fun insertAllEventTypes(vararg eventTypes: EventType): List<Long>

    @Insert
    fun insert(eventType: EventType): Long

    @Insert
    fun insertAllMatchEvents(vararg matchEvents: MatchEvent): List<Long>

    @Insert
    fun insert(matchEvent: MatchEvent): Long

    @Delete
    suspend fun delete(attribute: EventAttribute)
}

@Dao
interface EventJoinDao {

    @Transaction
    @Query("SELECT * FROM Player")
    suspend fun getPlayersForMatchEvents(): List<PlayersForMatchEvents>

    @Transaction
    @Query("SELECT * FROM MatchEvent")
    suspend fun getMatchEventsWithPlayers(): List<MatchEventsWithPlayers>

    @Transaction
    @Query("SELECT * FROM Attribute")
    suspend fun getAttributesForMatchEvent(): List<AttributesForMatchEvents>

    @Transaction
    @Query("SELECT * FROM MatchEvent")
    suspend fun getMatchEventsWithAttributes(): List<MatchEventsWithAttributes>

    @Insert
    fun insertAllEventPlayerJoins(vararg eventPlayerJoin: MatchEventPlayer): List<Long>

    @Insert
    fun insertAllEventAttributeJoins(vararg eventAttributeJoin: MatchEventAttribute): List<Long>
}