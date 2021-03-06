package de.js329.sportsvideotagging.controller

import de.js329.sportsvideotagging.database.*
import de.js329.sportsvideotagging.datamodels.*

class MatchTaggingController(
        private val matchDao: MatchDao,
        private val eventDao: EventDao,
        private val joinDao: EventJoinDao,
        private val teamDao: TeamDao,
        private val playerDao: PlayerDao
) {

    private var eventTypes: List<EventType> = ArrayList()
    private var eventAttributes: List<EventAttribute> = ArrayList()
    var match: Match? = null
    private var matchEvents: MutableList<MatchEvent> = ArrayList()
    private var latestMatchEvent: MatchEvent? = null
    var eventSequenceNum = 0
    lateinit var homeTeam: Team
    lateinit var guestTeam: Team

    suspend fun getTeamForName(teamName: String): Team {
        return teamDao.getTeamForName(teamName)
    }

    fun getEventType(): EventType? {
        latestMatchEvent?.let {
            return eventTypes.find { eventType -> eventType.uid == it.eventTypeId }
        }
        return null
    }

    suspend fun getEventTypes(): List<EventType> {
        if (eventTypes.isEmpty()) {
            eventTypes = eventDao.getAllEventTypes()
        }
        return eventTypes
    }

    suspend fun getLongTimedEventTypes(): List<LongTimedEventType> {
        return eventDao.getAllLongTimedEventTypes()
    }

    suspend fun getAllTeams(): List<Team> {
        return teamDao.getAll()
    }

    suspend fun getPlayersForTeam(team: Team?): List<Player> {
        team?.uid?.let {
            return playerDao.getPlayersForTeam(it)
        }
        return ArrayList()
    }

    suspend fun getEventAttributes(): List<EventAttribute> {
        if (eventAttributes.isEmpty()) {
            eventAttributes = eventDao.getAllAttributes()
        }
        return eventAttributes
    }

    private suspend fun assignTeams(homeTeamId: Long, guestTeamId: Long) {
        teamDao.getTeamForId(homeTeamId)?.let { homeTeam = it }
        teamDao.getTeamForId(guestTeamId)?.let { guestTeam = it }
    }

    suspend fun startMatch(homeTeamId: Long, guestTeamId: Long, matchDate: Long, timestamp: Long) {
        match = Match(null, matchDate, homeTeamId, guestTeamId, 0, 0)
        assignTeams(homeTeamId, guestTeamId)
        match?.let {
            val matchId = matchDao.insert(it)
            it.uid = matchId
            getEventTypes()
            eventTypes.first { eventType -> eventType.eventTitle == "Match Start" }.uid?.let { uid ->
                val startEvent = MatchEvent(
                        null,
                        matchId,
                        eventSequenceNum,
                        timestamp,
                        uid
                )
                latestMatchEvent = startEvent
                eventDao.insert(startEvent)
                addMatchEventToList()
            }
        }
    }

    fun createMatchEvent(eventType: EventType, timestamp: Long): Boolean {
        val matchId = match?.uid ?: return false
        val eventTypeId = eventType.uid ?: return false

        latestMatchEvent = MatchEvent(
                null,
                matchId,
                ++eventSequenceNum,
                timestamp + eventType.timeOffset,
                eventTypeId
        )
        return true
    }

    suspend fun createLongTimedMatchEvent(longTimedEventType: LongTimedEventType, timestamp: Long, isSwitched: Boolean) :Boolean {
        val matchId = match?.uid ?: return false
        val eventTypeId = longTimedEventType.uid ?: return false
        val longTimedMatchEvent = MatchLongTimedEvent(null, matchId, ++eventSequenceNum, timestamp, eventTypeId, isSwitched)
        longTimedMatchEvent.matchLongTimedEventId = eventDao.insert(longTimedMatchEvent)
        return true
    }

    fun finishMatchEventCreation() {
        addMatchEventToList()
    }

    suspend fun cancelEventCreation() {
        latestMatchEvent?.let { matchEvent ->
            if (matchEvent.matchEventId != null) {
                eventDao.delete(matchEvent)
            }
            deleteFollowingEventIfPresent()
        }
        eventSequenceNum -= 1
        latestMatchEvent = null
    }

    suspend fun deleteFollowingEventIfPresent() {
        latestMatchEvent?.let { matchEvent ->
            val followIngEventId = matchEvent.followingEventId
            followIngEventId?.let {
                matchEvent.followingEventId = null
                eventDao.update(matchEvent)
                eventDao.deleteMatchEventById(it)
                eventSequenceNum -= 1
            }
        }
    }

    suspend fun deleteLastMatchEvent() {
        val lastAddedEvent = matchEvents.removeLastOrNull()
        lastAddedEvent?.let { matchEvent ->
            eventDao.delete(matchEvent)
            eventSequenceNum -= 1
            matchEvent.followingEventId?.let {
                eventDao.deleteMatchEventById(it)
                eventSequenceNum -= 1
            }
        }
    }

    suspend fun addEventAttributes(attributes: List<EventAttribute>, matchEvent: MatchEvent? = latestMatchEvent) {
        matchEvent?.let {
            val matchEventId = returnMatchEventId(it)
            attributes.forEach { attribute ->
                val attributeId = attribute.attributeId ?: return@forEach
                val eventAttributeJoin = MatchEventAttribute(matchEventId, attributeId)
                joinDao.insertAllEventAttributeJoins(eventAttributeJoin)
            }
        }
    }

    suspend fun addEventPlayers(players: List<Player>, matchEvent: MatchEvent? = latestMatchEvent) {
        matchEvent?.let {
            val matchEventId = returnMatchEventId(it)
            players.forEach { player ->
                val playerId = player.playerId ?: return@forEach
                val eventPlayerJoin = MatchEventPlayer(matchEventId, playerId)
                joinDao.insertAllEventPlayerJoins(eventPlayerJoin)
            }
        }
    }

    suspend fun deleteEventPlayersForLatestMatchEvent() {
        latestMatchEvent?.let { matchEvent ->
            val matchEventId = returnMatchEventId(matchEvent)
            val playerIds = joinDao.getPlayerIdsForMatchEventId(matchEventId)
            playerIds.forEach {
                joinDao.deleteEventPlayerJoin(MatchEventPlayer(matchEventId, it))
            }
        }
    }

    suspend fun addFollowUpEvent(eventType: EventType, players: List<Player> = ArrayList(), attributes: List<EventAttribute> = ArrayList()): Boolean {
        latestMatchEvent?.let {
            val matchId = match?.uid ?: return false
            val eventTypeId = eventType.uid ?: return false

            val followUpEvent = MatchEvent(
                    null,
                    matchId,
                    ++eventSequenceNum,
                    it.eventTimestamp,
                    eventTypeId
            )

            if (players.isNotEmpty()) {
                addEventPlayers(players, followUpEvent)
            }

            if (attributes.isNotEmpty()) {
                addEventAttributes(attributes, followUpEvent)
            }

            val followUpMatchId = returnMatchEventId(followUpEvent)
            it.followingEventId = followUpMatchId

            return true
        }
        return false
    }

    private fun addMatchEventToList() {
        val matchEvent = latestMatchEvent ?: return
        matchEvents.add(matchEvent)
        latestMatchEvent = null
    }

    suspend fun endMatch(homeTeamScore: Int, guestTeamScore: Int) {
        match?.homeScore = homeTeamScore
        match?.guestScore = guestTeamScore
        match?.let { matchDao.updateAll(it) }
    }

    private suspend fun returnMatchEventId(matchEvent: MatchEvent): Long {
        matchEvent.matchEventId?.let { return it }
        val autoGeneratedId = eventDao.insert(matchEvent)
        matchEvent.matchEventId = autoGeneratedId
        return autoGeneratedId
    }
}