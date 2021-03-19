package de.js329.sportsvideotagging

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneOffset
import java.util.*
import kotlin.collections.ArrayList

class TaggingDatabaseTest {
    private lateinit var db: VideoTagDatabase

    private var sampleTeams: List<Team> = ArrayList()
    private var samplePlayers: List<Player> = ArrayList()
    private var sampleAttributes: List<EventAttribute> = ArrayList()
    private var sampleEventTypes: List<EventType> = ArrayList()
    private var sampleMatchEvents: List<MatchEvent> = ArrayList()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
                context, VideoTagDatabase::class.java).build()
    }

    @Before
    fun createSampleData() {
        sampleTeams = createSampleTeams()
        sampleAttributes = createSampleEventAttributes()
        sampleEventTypes = createSampleEventTypes()
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

    private fun insertSampleTeams() = runBlocking {
        if (sampleTeams[0].uid == null) {
            db.teamDao().insertAll(*sampleTeams.toTypedArray())
                .mapIndexed { list_index, db_index -> sampleTeams[list_index].uid = db_index }
        }
    }

    private fun createSampleEventAttributes(): List<EventAttribute> {
        val attributeA = EventAttribute(null, "Tor")
        val attributeB = EventAttribute(null, "Fehlwurf")
        return listOf(attributeA, attributeB)
    }

    private fun insertSampleAttributes() = runBlocking {
        db.eventDao().insertAllEventAttributes(*sampleAttributes.toTypedArray())
                .mapIndexed { list_index, db_index -> sampleAttributes[list_index].attributeId = db_index }
    }

    private fun createSampleEventTypes(): List<EventType> {
        val eventTypeA = EventType(null, "Torwurf", -5, playerSelection = true, attributesAllowed = true, activeEventType = true)
        val eventTypeB = EventType(null, "gute Defensivaktion", -20, false, attributesAllowed = false, activeEventType = true)
        return listOf(eventTypeA, eventTypeB)
    }

    private fun insertSampleEventTypes() = runBlocking {
        db.eventDao().insertAllEventTypes(*sampleEventTypes.toTypedArray())
                .mapIndexed { list_index, db_index -> sampleEventTypes[list_index].uid = db_index }
    }

    private fun createSamplePlayers(): List<Player> {
        insertSampleTeams()
        sampleTeams.forEach { team ->
            team.uid?.let {
                val playerA = Player(null, 8, it, "Max Mustermann")
                val playerB = Player(null, 9, it, "Hans Dampf")
                return listOf(playerA, playerB)
            }
        }
        return ArrayList()
    }

    private fun insertSamplePlayers() = runBlocking {
        db.playerDao().insertAll(*samplePlayers.toTypedArray())
                .mapIndexed { list_index, db_index -> samplePlayers[list_index].playerId = db_index }
    }

    private fun createSampleMatch(): Match? {
        insertSampleTeams()
        val homeTeamId = sampleTeams[0].uid ?: return null
        val guestTeamId = sampleTeams[1].uid ?: return null

        return Match(
                null,
                Calendar.getInstance().apply {
                    set(Calendar.YEAR, 2020)
                    set(Calendar.MONTH, Month.OCTOBER.value)
                    set(Calendar.DAY_OF_MONTH, 24)
                    set(Calendar.HOUR, 13)
                    set(Calendar.MINUTE, 38)
                    set(Calendar.SECOND, 22)
                }.time.time,
                homeTeamId,
                guestTeamId,
                27,
                24
        )
    }

    private fun createSampleMatchEvents(): List<MatchEvent> = runBlocking {
        val match = createSampleMatch()
        match?.let {
            it.uid = db.matchDao().insertAll(it)[0]
        }
        insertSampleEventTypes()

        val matchId = match?.uid ?: return@runBlocking ArrayList()
        val typeAId = sampleEventTypes[0].uid ?: return@runBlocking ArrayList()
        val typeBId = sampleEventTypes[1].uid ?: return@runBlocking ArrayList()

        val matchEventA = MatchEvent(
                null,
                matchId,
                1,
                LocalDateTime.of(2020, Month.OCTOBER, 24, 20, 31, 9).toEpochSecond(ZoneOffset.UTC),
                typeAId,
                null
        )
        val matchEventB = MatchEvent(
                null,
                matchId,
                2,
                LocalDateTime.of(2020, Month.OCTOBER, 24, 20, 31, 53).toEpochSecond(ZoneOffset.UTC),
                typeBId,
                null
        )
        return@runBlocking listOf(matchEventA, matchEventB)
    }

    private fun insertSampleMatchEvents() = runBlocking {
        db.eventDao().insertAllMatchEvents(*sampleMatchEvents.toTypedArray())
                .mapIndexed { list_index, db_index -> sampleMatchEvents[list_index].matchEventId = db_index }
    }

    private fun createSampleEventPlayerJoin(): MatchEventPlayer? {
        sampleMatchEvents = createSampleMatchEvents()
        insertSampleMatchEvents()
        samplePlayers = createSamplePlayers()
        insertSamplePlayers()
        val matchEventId = sampleMatchEvents[0].matchEventId ?: return null
        val playerId = samplePlayers[0].playerId ?: return null
        return MatchEventPlayer(matchEventId, playerId)
    }

    private fun insertSampleEventPlayerJoin(matchEventPlayer: MatchEventPlayer) = runBlocking {
        db.eventJoinDao().insertAllEventPlayerJoins(matchEventPlayer)
    }

    private fun createSampleEventAttributeJoin(): MatchEventAttribute? {
        sampleMatchEvents = createSampleMatchEvents()
        insertSampleMatchEvents()
        insertSampleAttributes()
        val matchEventId = sampleMatchEvents[0].matchEventId ?: return null
        val attributeId = sampleAttributes[0].attributeId ?: return null
        return MatchEventAttribute(matchEventId, attributeId)
    }

    private fun insertSampleEventAttributeJoin(matchEventAttribute: MatchEventAttribute) = runBlocking {
        db.eventJoinDao().insertAllEventAttributeJoins(matchEventAttribute)
    }

    @Test
    fun checkEmptyDatabase() = runBlocking {
        val teams = db.teamDao().getAll()
        Assert.assertEquals(0, teams.size)
    }

    @Test
    fun teamInsertAndGet() = runBlocking {
        insertSampleTeams()
        val teams = db.teamDao().getAll()
        Assert.assertEquals(sampleTeams.size, teams.size)
    }

    @Test
    fun attributesInsertAndGet() = runBlocking {
        insertSampleAttributes()
        val attributes = db.eventDao().getAllAttributes()
        Assert.assertEquals(sampleAttributes.size, attributes.size)
    }

    @Test
    fun eventTypesInsertAndGet() = runBlocking {
        insertSampleEventTypes()
        val eventTypes = db.eventDao().getAllEventTypes()
        Assert.assertEquals(sampleEventTypes.size, eventTypes.size)
    }

    @Test
    fun playersInsertAndGet() = runBlocking {
        val existingPlayers = samplePlayers.size
        samplePlayers = createSamplePlayers()
        insertSamplePlayers()
        val players = db.playerDao().getAll()
        Assert.assertTrue(samplePlayers.size > existingPlayers)
        Assert.assertEquals(samplePlayers.size, players.size)
    }

    @Test
    fun matchInsertAndGet() = runBlocking {
        createSampleMatch()?.let {
            it.uid = db.matchDao().insertAll(it)[0]
        }
        val matches = db.matchDao().getAll()
        Assert.assertEquals(1, matches.size)
    }

    @Test
    fun matchEventsInsertAndGet() = runBlocking {
        val existingMatchEvent = sampleMatchEvents.size
        sampleMatchEvents = createSampleMatchEvents()
        insertSampleMatchEvents()
        val matchEvents = db.eventDao().getAllMatchEvents()
        Assert.assertTrue(sampleMatchEvents.size > existingMatchEvent)
        Assert.assertEquals(sampleMatchEvents.size, matchEvents.size)
    }

    @Test
    fun eventPlayerJoinInsertAndGet() = runBlocking {
        createSampleEventPlayerJoin()?.let {
            insertSampleEventPlayerJoin(it)
        }
        val matchEvents = db.eventJoinDao().getMatchEventsWithPlayers()
        val playerEvents = db.eventJoinDao().getPlayersForMatchEvents()
        val countEventsWithPlayers = matchEvents.count { it.players.isNotEmpty() }
        val countPlayersInEvents = playerEvents.count { it.matchEvents.isNotEmpty() }
        Assert.assertEquals(1, countEventsWithPlayers)
        Assert.assertEquals(1, countPlayersInEvents)
        Assert.assertEquals(matchEvents.first { it.players.isNotEmpty() }.players[0], playerEvents.first { it.matchEvents.isNotEmpty() }.player)
    }

    @Test
    fun eventAttributeInsertAndGet() = runBlocking {
        createSampleEventAttributeJoin()?.let {
            insertSampleEventAttributeJoin(it)
        }
        val matchEvents = db.eventJoinDao().getMatchEventsWithAttributes()
        val attributeEvents = db.eventJoinDao().getAttributesForMatchEvent()
        val countEventsWithAttributes = matchEvents.count { it.attributes.isNotEmpty() }
        val countAttributesInEvents = attributeEvents.count { it.matchEvents.isNotEmpty() }
        Assert.assertEquals(1, countAttributesInEvents)
        Assert.assertEquals(1, countEventsWithAttributes)
        Assert.assertEquals(matchEvents.first { it.attributes.isNotEmpty() }.attributes[0], attributeEvents.first { it.matchEvents.isNotEmpty() }.attribute)
    }
}