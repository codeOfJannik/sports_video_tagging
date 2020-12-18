package de.js329.sportsvideotagging.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
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
        ExportController(db.eventDao(), db.matchDao(), db.teamDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration_listview)

        findViewById<FloatingActionButton>(R.id.addFAB).visibility = View.GONE
        findViewById<TextView>(R.id.listViewHeader).text = getString(R.string.taggedMatches)

        setListData()
    }

    private fun setListData() {
        val matchesTagsPairs = ArrayList<Pair<Match, List<MatchEvent>>>()
        lifecycleScope.launch {
            val matches = exportController.getMatches()
            val teams = exportController.getTeams()
            matches.forEach { match ->
                match.uid?.let { uid ->
                    val events = exportController.getEventsForMatch(uid).toMutableList()
                    events.removeIf { it.matchEventOrderNumber == 0 }
                    matchesTagsPairs.add(Pair(match, events))
                }
                findViewById<ListView>(R.id.listView).adapter =
                        TaggedMatchesAdapter(this@TaggedMatchesOverviewActivity, matchesTagsPairs, teams)
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