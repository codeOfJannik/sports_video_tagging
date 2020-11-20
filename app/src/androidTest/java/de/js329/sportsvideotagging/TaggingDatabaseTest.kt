package de.js329.sportsvideotagging

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.Team
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException

class TaggingDatabaseTest {
    private lateinit var db: VideoTagDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, VideoTagDatabase::class.java).build()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    private fun createSampleTeams(): List<Team> {
        val teamA = Team(null, "HSG Ostfildern")
        val teamB = Team(null, "SG Hegensberg-Liebersbronn")
        return listOf(teamA, teamB)
    }

    @Test
    fun teamInsertAndGet() {
        val sampleTeams = createSampleTeams()
        db.teamDao().insertAll(*sampleTeams.toTypedArray()).mapIndexed { list_index, db_index -> sampleTeams[list_index].uid = db_index }
        val teams = db.teamDao().getAll()
        teams.map { team -> println("${team.uid}: ${team.teamName}")}
        sampleTeams.map { team -> println("${team.uid}: ${team.teamName}")}
        Assert.assertEquals(sampleTeams.size, teams.size)
    }
}