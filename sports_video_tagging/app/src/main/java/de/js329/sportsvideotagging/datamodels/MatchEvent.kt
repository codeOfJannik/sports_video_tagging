package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "MatchEvent",
    foreignKeys = [
        ForeignKey(entity = Match::class, parentColumns = ["uid"], childColumns = ["match"], onDelete = CASCADE),
        ForeignKey(entity = MatchEvent::class, parentColumns = ["matchEventId"], childColumns = ["following_event"], onDelete = CASCADE),
        ForeignKey(entity = EventType::class, parentColumns = ["uid"], childColumns = ["event_type"], onDelete = CASCADE)
    ]
)
data class MatchEvent (
        @PrimaryKey(autoGenerate = true) var matchEventId: Long? = null,
        @ColumnInfo(name = "match") val matchId: Long,
        @ColumnInfo(name = "match_order_number") val matchEventOrderNumber: Int,
        @ColumnInfo(name = "timestamp") val eventTimestamp: Long,
        @ColumnInfo(name = "event_type") val eventTypeId: Long,
        @ColumnInfo(name = "following_event") var followingEventId: Long? = null
)