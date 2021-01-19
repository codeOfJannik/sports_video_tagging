package de.js329.sportsvideotagging.controller

import de.js329.sportsvideotagging.database.*
import de.js329.sportsvideotagging.datamodels.*

class ExportController(
    private val playerDao: PlayerDao,
    private val eventDao: EventDao,
    private val matchDao: MatchDao,
    private val teamDao: TeamDao,
    private val eventJoinDao: EventJoinDao
) {

    lateinit var eventTypes: List<EventType>
    lateinit var longTimedEventTypes: List<LongTimedEventType>
    val activeLongTimedEvents: MutableList<MatchLongTimedEvent> = ArrayList()

    fun longTimedEventHandler(event: MatchLongTimedEvent, eventType: LongTimedEventType) {
        val activeEvent = activeLongTimedEvents.firstOrNull { it.eventTypeId == event.eventTypeId }
        activeEvent?.let {
            if (eventType.switchable) {
                activeLongTimedEvents.add(event)
            }
            activeLongTimedEvents.remove(activeEvent)
        }
        activeLongTimedEvents.add(event)
    }

    suspend fun getMatches(): List<Match> {
        return matchDao.getAll()
    }

    suspend fun getTeams(): List<Team> {
        return teamDao.getAll()
    }

    suspend fun getEventsForMatch(matchId: Long): List<MatchEvent> {
        return eventDao.getMatchEventsForMatch(matchId)
    }

    suspend fun getTeamForId(teamId: Long): Team? {
        return teamDao.getTeamForId(teamId)
    }

    suspend fun getLongTimedEventsForMatch(matchId: Long): List<MatchLongTimedEvent> {
        return eventDao.getLongTimedMatchEventsForMatch(matchId)
    }

    suspend fun getLongTimedEventTypeById(longTimedEventId: Long): LongTimedEventType? {
        return eventDao.getLongTimedEventTypeForId(longTimedEventId)
    }

    fun getEventTypeById(eventTypeId: Long): EventType? {
        return eventTypes.firstOrNull { it.uid == eventTypeId }
    }

    suspend fun queryEventTypes() {
        eventTypes = eventDao.getAllEventTypes()
        longTimedEventTypes = eventDao.getAllLongTimedEventTypes()
    }

    suspend fun deleteMatchWithAllTags(match: Match) {
        matchDao.delete(match)
    }

    suspend fun getAttributesForMatchEvent(matchEvent: MatchEvent): List<EventAttribute> {
        matchEvent.matchEventId?.let {
            return eventJoinDao.getAttributesForMatchEventWithId(it).attributes
        }
        return ArrayList()
    }

    suspend fun getPlayersForTeamOfMatchEvent(matchEvent: MatchEvent, teamId: Long): List<Player> {
        matchEvent.matchEventId?.let {
            val playerIds = eventJoinDao.getPlayerIdsForMatchEventId(it)
            return playerDao.getPlayersWithIdsForTeam(playerIds, teamId)
        }
        return ArrayList()
    }

}