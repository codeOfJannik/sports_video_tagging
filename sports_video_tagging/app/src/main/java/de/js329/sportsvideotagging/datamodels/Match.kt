package de.js329.sportsvideotagging.datamodels

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(entity = Team::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("home_team"), onDelete = CASCADE),
        ForeignKey(entity = Team::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("guest_team"), onDelete = CASCADE)
    ],
    tableName = "Match")
data class Match(
        @PrimaryKey(autoGenerate = true) var uid: Long? = null,
        @ColumnInfo(name = "date") var date: Long? = null,
        @ColumnInfo(name = "home_team") val homeTeamId: Long,
        @ColumnInfo(name = "guest_team") val guestTeamId: Long,
        @ColumnInfo(name = "home_score") var homeScore: Int,
        @ColumnInfo(name = "guest_score") var guestScore: Int
)