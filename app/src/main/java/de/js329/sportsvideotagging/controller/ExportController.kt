package de.js329.sportsvideotagging.controller

import de.js329.sportsvideotagging.database.EventDao
import de.js329.sportsvideotagging.database.MatchDao
import de.js329.sportsvideotagging.database.TeamDao
import de.js329.sportsvideotagging.datamodels.Match
import de.js329.sportsvideotagging.datamodels.MatchEvent
import de.js329.sportsvideotagging.datamodels.Team

class ExportController(
        private val eventDao: EventDao,
        private val matchDao: MatchDao,
        private val teamDao: TeamDao
) {
    suspend fun getMatches(): List<Match> {
        return matchDao.getAll()
    }

    suspend fun getTeams(): List<Team> {
        return teamDao.getAll()
    }

    suspend fun getEventsForMatch(matchId: Long): List<MatchEvent> {
        return eventDao.getMatchEventsForMatch(matchId)
    }
}