package de.js329.sportsvideotagging.activities

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ExportController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.Match
import de.js329.sportsvideotagging.datamodels.MatchEvent
import de.js329.sportsvideotagging.datamodels.Team
import de.js329.sportsvideotagging.toFormattedString
import kotlinx.coroutines.launch
import java.util.*

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
        // TODO: handle export here
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