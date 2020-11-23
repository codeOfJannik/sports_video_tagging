package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "EventType")
data class EventType (
        @PrimaryKey(autoGenerate = true) var uid: Long? = null,
        @ColumnInfo(name = "event_title") val eventTitle: String,
        @ColumnInfo(name = "long_time_event") val longTimedEvent: Boolean,
        @ColumnInfo(name = "time_offset") val timeOffset: Long,
        @ColumnInfo(name = "player_selection") val playerSelection: Boolean,
) {
}