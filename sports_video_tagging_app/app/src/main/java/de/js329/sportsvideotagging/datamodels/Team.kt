package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["team_name"], unique = true)], tableName = "Team")
data class Team (
    @PrimaryKey(autoGenerate = true) var uid: Long? = null,
    @ColumnInfo(name = "team_name") var teamName: String
)