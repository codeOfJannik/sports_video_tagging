package de.js329.sportsvideotagging.datamodels

import androidx.room.*

@Entity(
    tableName = "Player",
    foreignKeys = [
        ForeignKey(entity = Team::class, parentColumns = arrayOf("uid"), childColumns = arrayOf("team"))
    ],
    indices = [
        Index(value = arrayOf("number", "team", "name"), unique = true)
    ]
)
data class Player (
    @PrimaryKey(autoGenerate = true) val playerId: Long,
    @ColumnInfo(name = "number") var number: Int,
    @ColumnInfo(name = "team") val team_id: Long,
    @ColumnInfo(name = "name") val name: String? = null
) {
}