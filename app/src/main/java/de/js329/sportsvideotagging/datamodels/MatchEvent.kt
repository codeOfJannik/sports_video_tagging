package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "match_events",
    foreignKeys = [
        ForeignKey(entity = Match::class, parentColumns = ["uid"], childColumns = ["match"]),
        ForeignKey(entity = MatchEvent::class, parentColumns = ["uid"], childColumns = ["following_event"])
    ]
)
data class MatchEvent (
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    @ColumnInfo(name = "match") val match_id: Long,
    @ColumnInfo(name = "match_order_number") val match_event_order_number: Int,
    @ColumnInfo(name = "timestamp") val event_timestamp: Long,
    @ColumnInfo(name = "event_type") val event_id: Long,
    @ColumnInfo(name = "following_event") val eventid: Long? = null
) {
}