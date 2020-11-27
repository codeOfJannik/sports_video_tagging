package de.js329.sportsvideotagging.datamodels

import androidx.room.*

@Entity(
    tableName = "MatchEventPlayerJoin",
    primaryKeys = ["matchEventId", "playerId"]
)
data class MatchEventPlayer (
        val matchEventId: Long,
        val playerId: Long
)

data class MatchEventsWithPlayers(
    @Embedded val matchEvent: MatchEvent,
    @Relation(
        parentColumn = "matchEventId",
        entityColumn = "playerId",
        associateBy = Junction(MatchEventPlayer::class)
    )
    val players: List<Player>
)

data class PlayersForMatchEvents(
    @Embedded val player: Player,
    @Relation(
        parentColumn = "playerId",
        entityColumn = "matchEventId",
        associateBy = Junction(MatchEventPlayer::class)
    )
    val matchEvents: List<MatchEvent>
)