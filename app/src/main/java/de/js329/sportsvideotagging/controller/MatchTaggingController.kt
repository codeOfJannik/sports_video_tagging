package de.js329.sportsvideotagging.controller

import de.js329.sportsvideotagging.database.*
import de.js329.sportsvideotagging.datamodels.*
import java.time.LocalDateTime
import java.time.ZoneOffset

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
    var matchEvents: MutableList<MatchEvent> = ArrayList()
    var latestMatchEvent: MatchEvent? = null
    var eventOrderNum = 0
    lateinit var homeTeam: Team
    lateinit var awayTeam: Team

    suspend fun getTeamForName(teamName: String): Team {
        return teamDao.getTeamForName(teamName)
    }

    suspend fun getEventTypes(): List<EventType> {
        if (eventTypes.isEmpty()) {
            eventTypes = eventDao.getAllEventTypes()
        }
        return eventTypes
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

    private suspend fun assignTeams(homeTeamId: Long, awayTeamId: Long) {
        teamDao.getTeamForId(homeTeamId)?.let { homeTeam = it }
        teamDao.getTeamForId(awayTeamId)?.let { awayTeam = it }
    }

    suspend fun startMatch(homeTeamId: Long, awayTeamId: Long, timestamp: Long) {
        match = Match(null, null, homeTeamId, awayTeamId, 0, 0)
        assignTeams(homeTeamId, awayTeamId)
        match?.let {
            val matchId = matchDao.insert(it)
            it.uid = matchId
            getEventTypes()
            eventTypes.first { eventType -> eventType.eventTitle == "Match Start" }.uid?.let { uid ->
                latestMatchEvent = MatchEvent(
                        null,
                        matchId,
                        eventOrderNum,
                        timestamp,
                        uid
                )
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
                ++eventOrderNum,
                timestamp,
                eventTypeId
        )
        return true
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

    suspend fun addFollowUpEvent(eventType: EventType, players: List<Player> = ArrayList(), attributes: List<EventAttribute> = ArrayList()): Boolean {
        latestMatchEvent?.let {
            val matchId = match?.uid ?: return false
            val eventTypeId = eventType.uid ?: return false

            val followUpEvent = MatchEvent(
                    null,
                    matchId,
                    ++eventOrderNum,
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

    suspend fun endMatch(homeTeamScore: Int, awayTeamScore: Int) {
        match?.homeScore = homeTeamScore
        match?.awayScore = awayTeamScore
        match?.let { matchDao.updateAll(it) }
    }

    private suspend fun returnMatchEventId(matchEvent: MatchEvent): Long {
        matchEvent.matchEventId?.let { return it }
        val autoGeneratedId = eventDao.insert(matchEvent)
        matchEvent.matchEventId = autoGeneratedId
        return autoGeneratedId
    }
}