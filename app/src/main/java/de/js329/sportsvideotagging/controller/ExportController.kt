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

    val activeLongTimedEvents: MutableSet<LongTimedEventType> = HashSet()

    suspend fun getMatches(): List<Match> {
        return matchDao.getAll()
    }

    suspend fun getTeams(): List<Team> {
        return teamDao.getAll()
    }

    suspend fun getMatchForId(matchId: Long): Match {
        return matchDao.getMatchForId(matchId)
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