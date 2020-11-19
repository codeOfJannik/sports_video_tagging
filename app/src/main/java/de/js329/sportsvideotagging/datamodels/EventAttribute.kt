package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Attribute")
data class EventAttribute (
    @PrimaryKey(autoGenerate = true) var attributeId: Long? = null,
    @ColumnInfo(name = "attribute_name") val attribute_name: String
) {
}