package de.js329.sportsvideotagging.controller

import de.js329.sportsvideotagging.database.EventDao
import de.js329.sportsvideotagging.database.PlayerDao
import de.js329.sportsvideotagging.database.TeamDao
import de.js329.sportsvideotagging.datamodels.EventAttribute
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.datamodels.Player
import de.js329.sportsvideotagging.datamodels.Team

class ConfigurationController(
        private val eventDao: EventDao,
        private val playerDao: PlayerDao,
        private val teamDao: TeamDao
) {

    fun addPlayer(name: String, number: Int, teamId: Long) {
        val player = Player(null, number, teamId)
        player.playerId = playerDao.insert(player)
    }

    suspend fun addTeam(teamName: String): Team {
        val team = Team(null, teamName)
        team.uid = teamDao.insert(team)
        return team
    }

    suspend fun deleteTeam(team: Team) {
        teamDao.delete(team)
    }

    fun addEventAttribute(attributeName: String) {
        val attribute = EventAttribute(null, attributeName)
        attribute.attributeId = eventDao.insert(attribute)
    }

    fun addEventType(eventTitle: String, longTimedEvent: Boolean, timeOffset: Long, playerSelection: Boolean) {
        val eventType = EventType(null, eventTitle, longTimedEvent, timeOffset, playerSelection)
        eventType.uid = eventDao.insert(eventType)
    }

    suspend fun getAllTeams(): List<Team> {
        return teamDao.getAll()
    }

    suspend fun getPlayersForTeam(team: Team): List<Player> {
        team.uid?.let {
            return playerDao.getPlayersForTeam(it)
        } ?: return ArrayList()
    }
}