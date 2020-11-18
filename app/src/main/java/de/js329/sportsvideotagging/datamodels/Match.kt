package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(
    foreignKeys = [
        ForeignKey(entity = Team::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("home_team")),
        ForeignKey(entity = Team::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("away_team"))
    ],
    tableName = "Match")
data class Match(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "date") var date: OffsetDateTime? = null,
    @ColumnInfo(name = "home_team") val home_team_id: Long,
    @ColumnInfo(name = "away_team") val away_team_id: Long,
    @ColumnInfo(name = "home_score") val home_score: Int,
    @ColumnInfo(name = "away_score") val away_score: Int
) {
}