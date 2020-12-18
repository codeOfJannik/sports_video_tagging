package de.js329.sportsvideotagging.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.activities.TaggingFragmentManager
import de.js329.sportsvideotagging.adapter.ExpandableRecyclerViewAdapter
import de.js329.sportsvideotagging.adapter.InnerRecyclerViewAdapter
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.datamodels.Player
import de.js329.sportsvideotagging.datamodels.Team
import kotlinx.coroutines.launch

class TaggingMatchEventPlayersFragment: Fragment(), InnerRecyclerViewAdapter.PlayerSelectionListener {

    private lateinit var matchTaggingController: MatchTaggingController
    private lateinit var expandableRecyclerView: RecyclerView
    private lateinit var taggingFragmentManager: TaggingFragmentManager

    private val teams: MutableList<Team> = ArrayList()
    private val players: MutableList<List<Player>> = ArrayList()
    private val selectedPlayers: MutableSet<Player> = HashSet()

    companion object {
        fun newInstance(matchTaggingController: MatchTaggingController, taggingFragmentManager: TaggingFragmentManager) = TaggingMatchEventPlayersFragment().apply {
            this.matchTaggingController = matchTaggingController
            this.taggingFragmentManager = taggingFragmentManager
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_match_event_players, container, false)
        view.findViewById<Button>(R.id.playerSelectionContinueBtn).setOnClickListener {
            lifecycleScope.launch {
                matchTaggingController.addEventPlayers(selectedPlayers.toList())
                taggingFragmentManager.switchToFinalizeMatchInput()
            }
        }
        expandableRecyclerView = view.findViewById(R.id.recyclerView)
        initiateAdapter()
        return view
    }

    private fun initiateAdapter() {
        val homeTeam = matchTaggingController.homeTeam
        val awayTeam = matchTaggingController.awayTeam
        teams.add(homeTeam)
        teams.add(awayTeam)

        lifecycleScope.launch {
            val homeTeamPlayers = matchTaggingController.getPlayersForTeam(homeTeam).sortedBy { it.number }
            players.add(homeTeamPlayers)
            val awayTeamPlayers = matchTaggingController.getPlayersForTeam(awayTeam).sortedBy { it.number }
            players.add(awayTeamPlayers)
            expandableRecyclerView.adapter = ExpandableRecyclerViewAdapter(requireContext(), teams, players, this@TaggingMatchEventPlayersFragment)
            expandableRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onPlayerSelected(player: Player) {
        selectedPlayers.add(player)
    }

    override fun onPlayerUnselected(player: Player) {
        selectedPlayers.remove(player)
    }
}