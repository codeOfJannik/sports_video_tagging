package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "LongTimedEventType")
data class LongTimedEventType (
        @PrimaryKey(autoGenerate = true) var uid: Long? = null,
        @ColumnInfo(name = "switchable") val switchable: Boolean,
        @ColumnInfo(name = "eventA_title") val eventATitle: String,
        @ColumnInfo(name = "eventB_title") val eventBTitle: String?,
        @ColumnInfo(name = "active_type") val activeEventType: Boolean


)