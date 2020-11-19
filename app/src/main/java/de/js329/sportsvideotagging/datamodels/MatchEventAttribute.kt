package de.js329.sportsvideotagging.datamodels

import androidx.room.*

@Entity(tableName = "MatchEventAttributeJoin")
data class MatchEventAttribute (
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    val match_event_id: Long,
    val attributeId: Long
)

data class MatchEventsWithAttributes (
    @Embedded val matchEvent: MatchEvent,
    @Relation(
        parentColumn = "uid",
        entityColumn = "attributeId",
        associateBy = Junction(MatchEventAttribute::class)
    )
    val attributes: List<EventAttribute>
)

data class AttributesForMatchEvents (
    @Embedded val attribute: EventAttribute,
    @Relation(
        parentColumn = "attributeId",
        entityColumn = "match_event_id",
        associateBy = Junction(MatchEventAttribute::class)
    )
    val matchEvents: List<MatchEvent>
)