package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(entity = Team::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("home_team"), onDelete = CASCADE),
        ForeignKey(entity = Team::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("away_team"), onDelete = CASCADE)
    ],
    tableName = "Match")
data class Match(
        @PrimaryKey(autoGenerate = true) var uid: Long? = null,
        @ColumnInfo(name = "date") var date: Long? = null,
        @ColumnInfo(name = "home_team") val homeTeamId: Long,
        @ColumnInfo(name = "away_team") val awayTeamId: Long,
        @ColumnInfo(name = "home_score") var homeScore: Int,
        @ColumnInfo(name = "away_score") var awayScore: Int
)