package de.js329.sportsvideotagging.activities

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ExportController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.*
import de.js329.sportsvideotagging.toFormattedString
import kotlinx.coroutines.*
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml
import java.io.PrintWriter
import java.util.*
import kotlin.collections.ArrayList

class TaggedMatchesOverviewActivity : AppCompatActivity() {

    companion object {
        private const val CREATE_EXPORT_FILE = 1
    }

    var matchSelectedForExport: Match? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == CREATE_EXPORT_FILE) {
            val uri = data?.data ?: return
            matchSelectedForExport?.let {
                val outputStream = contentResolver.openOutputStream(uri) ?: return
                GlobalScope.launch(Dispatchers.Main) {
                    val xmlString = exportToSVT(it)
                    xmlString?.let {
                        PrintWriter(outputStream).apply {
                            print(it)
                            close()
                            Toast.makeText(
                                    this@TaggedMatchesOverviewActivity,
                                    "Successfully exported to selected directory",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

        }
    }

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
                    events.removeIf { it.matchEventSequenceNumber == 0 }
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
                            matchSelectedForExport = match
                            val filename = getExportFileNameForMatch(match)
                            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type= "text/svt"
                                putExtra(Intent.EXTRA_TITLE, filename)
                            }
                            startActivityForResult(intent, CREATE_EXPORT_FILE)
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

    private fun getExportFileNameForMatch(match: Match): String {
        var filename = ""
        match.date?.let {
            val date = Calendar.getInstance()
            date.time = Date(it)
            filename = String.format("%d%02d%02d", date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
            filename += String.format("_%02d%02d_", date.get(Calendar.HOUR_OF_DAY), date.get(Calendar.MINUTE))
        }
        return filename + String.format("%d_matchExport.svt" ,match.uid)
    }

    private suspend fun exportToSVT(match: Match): String? {
        match.uid?.let { matchId ->
            exportController.reset()
            var allEvents: List<Pair<HashMap<String, Any>, Any>> = ArrayList()
            val matchEventData: MutableList<Pair<HashMap<String, Any>, Any>> = ArrayList()
            val longTimedEventData: MutableList<Pair<HashMap<String, Any>, LongTimedEventType>> =
                ArrayList()
            var homeTeam: Team? = null
            var guestTeam: Team? = null
            val worker = lifecycleScope.async {
                exportController.queryEventTypes()
                homeTeam = exportController.getTeamForId(match.homeTeamId)
                guestTeam = exportController.getTeamForId(match.guestTeamId)
                val matchEvents = exportController.getEventsForMatch(matchId)
                val longTimedMatchEvents = exportController.getLongTimedEventsForMatch(matchId)
                for (event in matchEvents) {
                    val eventType = exportController.getEventTypeById(event.eventTypeId)
                    val attributes = exportController.getAttributesForMatchEvent(event)
                    val homePlayers =
                        exportController.getPlayersForTeamOfMatchEvent(event, match.homeTeamId)
                    val guestPlayers =
                        exportController.getPlayersForTeamOfMatchEvent(event, match.guestTeamId)

                    /* Each element of variable data will look like:
                    Pair(
                        [first]: {
                            “event”: MatchEvent(),
                            “attributes”: [MatchEventAttribute(), MatchEventAttribute(), …] 
                            “homePlayers”: [Player(), Player(), …],
                            “guestPlayers”: [Player(), Player(), …]
                        },
                        [second]: EventType()
                    )
                    */
                    eventType?.let {
                        val data = Pair(
                            hashMapOf(
                                "event" to event,
                                "attributes" to attributes,
                                "homePlayers" to homePlayers,
                                "guestPlayers" to guestPlayers
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
                allEvents = matchEventData + longTimedEventData
            }

            print(worker.await())
            val sortedEvents = allEvents.sortedBy {
                when (val event = it.first["event"]) {
                    is MatchEvent -> {
                        event.matchEventSequenceNumber
                    }
                    is MatchLongTimedEvent -> {
                        event.matchLongTimedEventSequenceNumber
                    }
                    else -> {
                        allEvents.size * 2
                    }
                }
            }
            return writeXMLString(match, homeTeam, guestTeam, sortedEvents)
        }
        return null
    }

    private fun writeXMLString(match: Match, homeTeam: Team?, guestTeam: Team?, sortedEvents: List<Pair<HashMap<String, Any>, Any>>): String {
        var sequenceNum = 0
        var startTimeStamp = 0L
        val svt = xml("svt") {
            "match" {
                "metadata" {
                    "matchDateTime" {
                        match.date?.let { -Date(it).toString() }
                    }
                    "homeTeamScore" {
                        -match.homeScore.toString()
                    }
                    "guestTeamScore" {
                        -match.guestScore.toString()
                    }
                }
                "homeTeam" {
                    -homeTeam?.teamName.toString()
                }
                "guestTeam" {
                    -guestTeam?.teamName.toString()
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
                                val guestPlayers = eventData.first["guestPlayers"]

                                if (event.matchEventSequenceNumber == 0) {
                                    startTimeStamp = event.eventTimestamp
                                }
                                "matchEvent" {
                                    if (eventType is EventType) {
                                        attribute("eventTitle", eventType.eventTitle)
                                    }
                                    attribute(
                                        "matchEventSequenceNum",
                                        sequenceNum++
                                    )
                                    attribute(
                                        "matchEventTimeOffset",
                                        event.eventTimestamp - startTimeStamp
                                    )
                                    if (attributes is List<*> && attributes.isNotEmpty()) {
                                        "eventAttributes" {
                                            for (attribute in attributes.filterIsInstance<EventAttribute>()) {
                                                "attribute" { -attribute.attribute_name }
                                            }
                                            if (exportController.activeLongTimedEvents.isNotEmpty()) {
                                                for (name in exportController.getActiveLongTimedEventNames()) {
                                                    "attribute" { -name }
                                                }
                                            }
                                        }
                                    } else if (exportController.activeLongTimedEvents.isNotEmpty()) {
                                        "eventAttributes" {
                                            for (name in exportController.getActiveLongTimedEventNames()) {
                                                "attribute" { -name }
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
                                        if (guestPlayers is List<*> && guestPlayers.isNotEmpty()) {
                                            "guestTeamPlayers" {
                                                for (player in guestPlayers.filterIsInstance<Player>()) {
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
        return svt.toString(PrintOptions(
            pretty = true,
            singleLineTextElements = true,
            useSelfClosingTags = true
        ))
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
        val guestTeam = teams.find { it.uid == match.first.guestTeamId }
        view.findViewById<TextView>(R.id.matchDateTextView).text = Calendar.getInstance().apply {
            match.first.date?.let {
                time = Date(it)
            }
        }.toFormattedString()
        view.findViewById<TextView>(R.id.homeTeamNameTextView).text = homeTeam?.teamName ?: "No team name"
        view.findViewById<TextView>(R.id.guestTeamNameTextView).text = guestTeam?.teamName ?: "No team name"
        view.findViewById<TextView>(R.id.homeTeamScoreTextView).text = match.first.homeScore.toString()
        view.findViewById<TextView>(R.id.guestTeamScoreTextView).text = match.first.guestScore.toString()
        view.findViewById<TextView>(R.id.numberOfTagsTextView).text = String.format(Locale.getDefault(), "Tags for match: %d", match.second.size)
        return view
    }

}
