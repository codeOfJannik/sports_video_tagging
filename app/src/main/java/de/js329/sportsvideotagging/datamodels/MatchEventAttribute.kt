package de.js329.sportsvideotagging.datamodels

import androidx.room.*

@Entity(
    tableName = "MatchEventAttributeJoin",
    primaryKeys = ["matchEventId", "attributeId"]
)
data class MatchEventAttribute (
        val matchEventId: Long,
        val attributeId: Long
)

data class MatchEventsWithAttributes (
    @Embedded val matchEvent: MatchEvent,
    @Relation(
        parentColumn = "matchEventId",
        entityColumn = "attributeId",
        associateBy = Junction(MatchEventAttribute::class)
    )
    val attributes: List<EventAttribute>
)

data class AttributesForMatchEvents (
    @Embedded val attribute: EventAttribute,
    @Relation(
        parentColumn = "attributeId",
        entityColumn = "matchEventId",
        associateBy = Junction(MatchEventAttribute::class)
    )
    val matchEvents: List<MatchEvent>
)