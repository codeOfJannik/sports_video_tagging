package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "EventType")
data class EventType (
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    @ColumnInfo(name = "event_title") val event_title: String,
    @ColumnInfo(name = "long_time_event") val long_timed_event: Boolean,
    @ColumnInfo(name = "time_offset") val time_offset: Long,
    @ColumnInfo(name = "player_selection") val player_selection: Boolean,
) {
}