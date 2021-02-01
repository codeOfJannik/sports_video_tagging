package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
        tableName = "MatchLongTimedEvent",
        foreignKeys = [
            ForeignKey(entity = Match::class, parentColumns = ["uid"], childColumns = ["match"], onDelete = CASCADE),
            ForeignKey(entity = LongTimedEventType::class, parentColumns = ["uid"], childColumns = ["event_type"], onDelete = CASCADE)
        ]
)
data class MatchLongTimedEvent (
        @PrimaryKey(autoGenerate = true) var matchLongTimedEventId: Long? = null,
        @ColumnInfo(name = "match") val matchId: Long,
        @ColumnInfo(name = "match_order_number") val matchLongTimedEventOrderNumber: Int,
        @ColumnInfo(name = "timestamp") val eventTimestamp: Long,
        @ColumnInfo(name = "event_type") val eventTypeId: Long,
        @ColumnInfo(name = "switched_to_event_b") val switchedEvent: Boolean
)