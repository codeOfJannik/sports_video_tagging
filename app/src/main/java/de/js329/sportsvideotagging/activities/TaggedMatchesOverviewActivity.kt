package de.js329.sportsvideotagging.activities

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ExportController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.*
import de.js329.sportsvideotagging.toFormattedString
import kotlinx.coroutines.launch
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import java.util.*
import kotlin.collections.ArrayList

class TaggedMatchesOverviewActivity : AppCompatActivity() {

    private val exportController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        ExportController(db.playerDao(), db.eventDao(), db.matchDao(), db.teamDao(), db.eventJoinDao())
    }
    lateinit var taggedMatchesAdapter: TaggedMatchesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration_listview)

        findViewById<FloatingActionButton>(R.id.addFAB).visibility = View.GONE
        findViewById<TextView>(R.id.listViewHeader).text = getString(R.string.taggedMatches)

        setListData()
    }

    private fun setListData() {
        val matchesTagsPairs = ArrayList<Pair<Match, List<MatchEvent>>>()
        val matchesListview = findViewById<ListView>(R.id.listView)
        lifecycleScope.launch {
            val matches = exportController.getMatches()
            val teams = exportController.getTeams()
            matches.forEach { match ->
                match.uid?.let { uid ->
                    val events = exportController.getEventsForMatch(uid).toMutableList()
                    events.removeIf { it.matchEventOrderNumber == 0 }
                    matchesTagsPairs.add(Pair(match, events))
                }
                taggedMatchesAdapter = TaggedMatchesAdapter(this@TaggedMatchesOverviewActivity, matchesTagsPairs, teams)
                matchesListview.adapter = taggedMatchesAdapter
                matchesListview.setOnItemClickListener { _, _, position, _ ->
                    val clickedMatch = taggedMatchesAdapter.getItem(position).first
                    onTaggedMatchClicked(clickedMatch)
                }
            }
        }
    }

    private fun onTaggedMatchClicked(match: Match) {
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle(R.string.matchActionsTitle)
            .setNegativeButton(android.R.string.cancel) {dialog, _ -> dialog.dismiss() }
            .setItems(
                arrayOf("Delete Match", "Export in .svt file")
            ) { dialog, which ->
                when (which) {
                    0 -> {
                        onDeleteMatchClicked(match)
                        dialog.dismiss()
                    }
                    1 -> {
                        exportToSVT(match)
                        dialog.dismiss()
                    }
                }
            }
        val dialog = builder.create()
        dialog.listView.divider = ColorDrawable(resources.getColor(R.color.light_grey, null))
        dialog.listView.dividerHeight = 1
        dialog.show()
    }

    private fun onDeleteMatchClicked(match: Match) {
        val builder = AlertDialog.Builder(this)
        builder
            .setTitle(R.string.deleteTaggedMatchTitle)
            .setMessage(R.string.deleteTaggedMatchMessage)
            .setPositiveButton(android.R.string.ok) {_, _ ->
                lifecycleScope.launch {
                    exportController.deleteMatchWithAllTags(match)
                }
                val matches = taggedMatchesAdapter.matches.toMutableList()
                matches.removeIf { it.first == match }
                taggedMatchesAdapter.matches = matches
                taggedMatchesAdapter.notifyDataSetChanged()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss()}
            .show()
    }

    private fun exportToSVT(match: Match) {
        match.uid?.let { matchId ->
            lifecycleScope.launch {
                exportController.queryEventTypes()
                val homeTeam = exportController.getTeamForId(match.homeTeamId)
                val awayTeam = exportController.getTeamForId(match.awayTeamId)
                val matchEvents = exportController.getEventsForMatch(matchId)
                val matchEventData: MutableList<Pair<HashMap<String, Any>, Any>> = ArrayList()
                val longTimedMatchEvents = exportController.getLongTimedEventsForMatch(matchId)
                val longTimedEventData: MutableList<Pair<HashMap<String, Any>, LongTimedEventType>> = ArrayList()
                for (event in matchEvents) {
                    val eventType = exportController.getEventTypeById(event.eventTypeId)
                    val attributes = exportController.getAttributesForMatchEvent(event)
                    val homePlayers = exportController.getPlayersForTeamOfMatchEvent(event, match.homeTeamId)
                    val awayPlayers =  exportController.getPlayersForTeamOfMatchEvent(event, match.awayTeamId)
                    eventType?.let {
                        val data = Pair(
                            hashMapOf(
                                "event" to event,
                                "attributes" to attributes,
                                "homePlayers" to homePlayers,
                                "awayPlayers" to awayPlayers
                            ),
                            it
                        )
                        matchEventData.add(data)
                    }
                }
                for (event in longTimedMatchEvents) {
                    val eventType = exportController.getLongTimedEventTypeById(event.eventTypeId)
                    eventType?.let {
                        longTimedEventData.add(Pair(hashMapOf("event" to event), it))
                    }
                }
                val allEvents: List<Pair<HashMap<String, Any>, Any>> = matchEventData + longTimedEventData
                val sortedEvents = allEvents.sortedBy {
                    when (val event = it.first["event"]) {
                        is MatchEvent -> {
                            event.matchEventOrderNumber
                        }
                        is MatchLongTimedEvent -> {
                            event.matchLongTimedEventOrderNumber
                        }
                        else -> {
                            allEvents.size * 2
                        }
                    }
                }
                var orderNum = 0
                var startTimeStamp = 0L
                val svt = xml("svt") {
                    "match" {
                        "metadata" {
                            "matchDateTime" {
                                match.date?.let { Date(it).toString() }
                            }
                            "homeTeamScore" {
                                match.homeScore
                            }
                            "awayTeamScore" {
                                match.awayScore
                            }
                        }
                        "homeTeam" {
                            homeTeam?.teamName
                        }
                        "awayTeam" {
                            awayTeam?.teamName
                        }
                        "matchEvents" {
                            for (eventData in sortedEvents) {
                                val eventType = eventData.second
                                when (val event = eventData.first["event"]) {
                                    is MatchLongTimedEvent -> {
                                        if (eventType is LongTimedEventType) {
                                            exportController.longTimedEventHandler(event, eventType)
                                        }
                                        continue
                                    }
                                    is MatchEvent -> {
                                        val attributes = eventData.first["attributes"]
                                        val homePlayers = eventData.first["homePlayers"]
                                        val awayPlayers = eventData.first["awayPlayers"]

                                        if (event.matchEventOrderNumber == 0) {
                                            startTimeStamp = event.eventTimestamp
                                        }
                                        "matchEvent" {
                                            if (eventType is EventType) {
                                                attribute("eventTitle", eventType.eventTitle)
                                            }
                                            attribute(
                                                "matchEventOrderNum",
                                                orderNum++
                                            )
                                            attribute(
                                                "matchEventTimeOffset",
                                                event.eventTimestamp - startTimeStamp
                                            )
                                            if (attributes is List<*> && attributes.isNotEmpty()) {
                                                "eventAttributes" {
                                                    for (attribute in attributes.filterIsInstance<EventAttribute>()) {
                                                        "attribute" { attribute.attribute_name }
                                                    }
                                                }
                                            }
                                            "players" {
                                                if (homePlayers is List<*> && homePlayers.isNotEmpty()) {
                                                    "homeTeamPlayers" {
                                                        for (player in homePlayers.filterIsInstance<Player>()) {
                                                            "player" {
                                                                player.name?.let {
                                                                    attribute("playerName", it)
                                                                }
                                                                attribute(
                                                                    "jerseyNumber",
                                                                    player.number
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                if (awayPlayers is List<*> && awayPlayers.isNotEmpty()) {
                                                    "awayPlayers" {
                                                        for (player in awayPlayers.filterIsInstance<Player>()) {
                                                            "player" {
                                                                player.name?.let {
                                                                    attribute("playerName", it)
                                                                }
                                                                attribute("jerseyNumber", player.number)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                val xmlString = svt.toString(PrintOptions(
                    pretty = true,
                    singleLineTextElements = true,
                    useSelfClosingTags = true
                ))
            }
        }
    }
}

class TaggedMatchesAdapter(val context: Context, var matches: List<Pair<Match, List<MatchEvent>>>, val teams: List<Team>): BaseAdapter() {
    override fun getCount(): Int {
        return matches.size
    }

    override fun getItem(position: Int): Pair<Match, List<MatchEvent>> {
        return matches[position]
    }

    override fun getItemId(position: Int): Long {
        return matches[position].first.uid ?: -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.layout_tagged_match_listitem, parent, false)
        val match = matches[position]
        val homeTeam = teams.find { it.uid == match.first.homeTeamId }
        val awayTeam = teams.find { it.uid == match.first.awayTeamId }
        view.findViewById<TextView>(R.id.matchDateTextView).text = Calendar.getInstance().apply {
            match.first.date?.let {
                time = Date(it)
            }
        }.toFormattedString()
        view.findViewById<TextView>(R.id.homeTeamNameTextView).text = homeTeam?.teamName ?: "No team name"
        view.findViewById<TextView>(R.id.awayTeamNameTextView).text = awayTeam?.teamName ?: "No team name"
        view.findViewById<TextView>(R.id.homeTeamScoreTextView).text = match.first.homeScore.toString()
        view.findViewById<TextView>(R.id.awayTeamScoreTextView).text = match.first.awayScore.toString()
        view.findViewById<TextView>(R.id.numberOfTagsTextView).text = String.format(Locale.getDefault(), "Tags for match: %d", match.second.size)
        return view
    }

}