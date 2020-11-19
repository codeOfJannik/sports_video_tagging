package de.js329.sportsvideotagging.datamodels

import androidx.room.*

@Entity(tableName = "MatchEventPlayerJoin")
data class MatchEventPlayer (
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    val match_event_id: Long,
    val playerId: Long
)

data class MatchEventsWithPlayers(
    @Embedded val matchEvent: MatchEvent,
    @Relation(
        parentColumn = "uid",
        entityColumn = "playerId",
        associateBy = Junction(MatchEventPlayer::class)
    )
    val players: List<Player>
)

data class PlayersForMatchEvents(
    @Embedded val player: Player,
    @Relation(
        parentColumn = "playerId",
        entityColumn = "match_event_id",
        associateBy = Junction(MatchEventPlayer::class)
    )
    val matchEvents: List<MatchEvent>
)