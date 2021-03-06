package de.js329.sportsvideotagging.database

import androidx.room.*
import de.js329.sportsvideotagging.datamodels.*

@Dao
interface MatchDao {

    @Query("SELECT * FROM `Match`")
    suspend fun getAll(): List<Match>

    @Query("SELECT * FROM `Match` WHERE uid = :matchId")
    suspend fun getMatchForId(matchId: Long): Match

    @Insert
    suspend fun insertAll(vararg matches: Match): List<Long>

    @Insert
    suspend fun insert(match: Match): Long

    @Update
    suspend fun updateAll(vararg matches: Match)

    @Delete
    suspend fun delete(match: Match)
}

@Dao
interface PlayerDao {

    @Query("SELECT * FROM Player")
    suspend fun getAll(): List<Player>

    @Query("SELECT * FROM Player WHERE team = :teamId")
    suspend fun getPlayersForTeam(teamId: Long): List<Player>

    @Query("SELECT * FROM Player WHERE playerId IN (:playerIdList) AND team = :teamId")
    suspend fun getPlayersWithIdsForTeam(playerIdList: List<Long>, teamId: Long): List<Player>

    @Insert
    suspend fun insertAll(vararg players: Player): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: Player): Long

    @Update
    suspend fun updateAll(vararg players: Player)

    @Delete
    suspend fun delete(player: Player)
}

@Dao
interface TeamDao {

    @Query("SELECT * FROM Team")
    suspend fun getAll(): List<Team>

    @Query("SELECT * FROM Team WHERE uid = :teamId")
    suspend fun getTeamForId(teamId: Long): Team?

    @Query("SELECT * FROM Team WHERE team_name = :teamName")
    suspend fun getTeamForName(teamName: String): Team

    @Insert
    suspend fun insertAll(vararg teams: Team): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(team: Team): Long

    @Update
    suspend fun updateAll(vararg teams: Team)

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

    @Query("SELECT * FROM EventType WHERE uid = :id")
    suspend fun getEventTypeForId(id: Long): EventType?

    @Query("DELETE FROM MatchEvent WHERE matchEventId = :uid")
    suspend fun deleteMatchEventById(uid: Long)

    @Query("SELECT * FROM MatchEvent WHERE `match` = :matchId")
    suspend fun getMatchEventsForMatch(matchId: Long): List<MatchEvent>

    @Query("SELECT * FROM MatchLongTimedEvent WHERE `match` = :matchId")
    suspend fun getLongTimedMatchEventsForMatch(matchId: Long): List<MatchLongTimedEvent>

    @Query("SELECT * FROM LongTimedEventType WHERE uid = :eventTypeId")
    suspend fun getLongTimedEventTypeForId(eventTypeId: Long): LongTimedEventType?

    @Query("SELECT * FROM LongTimedEventType")
    suspend fun getAllLongTimedEventTypes(): List<LongTimedEventType>

    @Insert
    suspend fun insertAllEventAttributes(vararg attributes: EventAttribute): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(eventAttribute: EventAttribute): Long

    @Insert
    suspend fun insertAllEventTypes(vararg eventTypes: EventType): List<Long>

    @Insert
    suspend fun insertAllLongEventTypes(vararg longTimedEventType: LongTimedEventType): List<Long>

    @Insert
    suspend fun insert(eventType: EventType): Long

    @Insert
    suspend fun insertAllMatchEvents(vararg matchEvents: MatchEvent): List<Long>

    @Insert
    suspend fun insert(matchEvent: MatchEvent): Long

    @Insert
    suspend fun insert(matchLongTimedEvent: MatchLongTimedEvent): Long

    @Update
    suspend fun update(eventType: EventType)

    @Update
    suspend fun update(longTimedEventType: LongTimedEventType)

    @Update
    suspend fun update(matchEvent: MatchEvent)

    @Delete
    suspend fun delete(attribute: EventAttribute)

    @Delete
    suspend fun delete(eventType: EventType)

    @Delete
    suspend fun delete(matchEvent: MatchEvent)

    @Delete
    suspend fun delete(longTimedEventType: LongTimedEventType)
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
    @Query("SELECT * FROM MatchEvent WHERE matchEventId = :matchEventId")
    suspend fun getAttributesForMatchEventWithId(matchEventId: Long): MatchEventsWithAttributes

    @Transaction
    @Query("SELECT * FROM MatchEvent")
    suspend fun getMatchEventsWithAttributes(): List<MatchEventsWithAttributes>

    @Query("SELECT playerId FROM MatchEventPlayerJoin WHERE matchEventId = :id")
    suspend fun getPlayerIdsForMatchEventId(id: Long): List<Long>

    @Insert
    suspend fun insertAllEventPlayerJoins(vararg eventPlayerJoin: MatchEventPlayer): List<Long>

    @Insert
    suspend fun insertAllEventAttributeJoins(vararg eventAttributeJoin: MatchEventAttribute): List<Long>

    @Delete
    suspend fun deleteEventPlayerJoin(eventPlayerJoin: MatchEventPlayer)
}