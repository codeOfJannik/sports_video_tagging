package de.js329.sportsvideotagging.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ConfigurationController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.Team
import kotlinx.android.synthetic.main.activity_configuration_listview.*
import kotlinx.coroutines.launch

class EditTeamsActivity : AppCompatActivity() {

    private val configurationController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        ConfigurationController(db.eventDao(), db.playerDao(), db.teamDao())
    }

    private val allTeams: MutableList<Pair<Team, Int>> = ArrayList()
    private val teamAdapter by lazy {
        TeamAdapter(this, allTeams)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration_listview)


        val listView = findViewById<ListView>(R.id.listView)
        val addTeamFAB = findViewById<FloatingActionButton>(R.id.addFAB)
        findViewById<TextView>(R.id.listViewHeader).text = getString(R.string.editTeams)
        addTeamFAB.setOnClickListener {
            onAddTeamClicked()
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            onItemLongClick(position)
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            onItemClick(position)
        }
        listView.adapter = teamAdapter
    }

    override fun onStart() {
        super.onStart()
        queryTeamsWithPlayers()
    }

    private fun queryTeamsWithPlayers() {
        allTeams.clear()
        lifecycleScope.launch {
            val queriedTeams = configurationController.getAllTeams()
            queriedTeams.forEach { team ->
                val players = configurationController.getPlayersForTeam(team)
                allTeams.add(Pair(team, players.size))
            }
            updateList()
        }
    }

    private fun updateList() {
        teamAdapter.allTeams = allTeams
        teamAdapter.notifyDataSetChanged()
    }

    private fun onAddTeamClicked() {
        var teamName: String
        val builder = AlertDialog.Builder(this)

        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setTitle(R.string.addTeamAlertTitle_txt)
        builder.setMessage(R.string.addTeamAlertMessage_txt)

        builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
            teamName = input.text.toString()
            if (teamName.trim() != "") {
                lifecycleScope.launch {
                    val newTeam = configurationController.addTeam(teamName.trim())
                    newTeam?.let {
                        allTeams.add(Pair(it, 0))
                        updateList()
                    } ?: kotlin.run {
                        Toast.makeText(this@EditTeamsActivity, R.string.teamAlreadyExists, Toast.LENGTH_LONG).show()
                    }

                }
            } else {
                dialog.cancel()
                Toast.makeText(this, R.string.noTeamName, Toast.LENGTH_LONG).show()
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun onItemClick(position: Int): Boolean {
        val selectedTeam = teamAdapter.getItem(position)
        val intent = Intent(this, EditPlayersActivity::class.java)
        intent.putExtra("teamId", selectedTeam.first.uid)
        startActivity(intent)
        return true
    }

    private fun onItemLongClick(position: Int): Boolean {
        val teamPlayersPair = teamAdapter.getItem(position) as Pair<*, *>
        val team = teamPlayersPair.first as Team

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.deleteTeamTitle_txt)
        builder.setMessage(getString(R.string.deleteTeamMessage_txt, team.teamName))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            lifecycleScope.launch {configurationController.deleteTeam(team)}
            allTeams.remove(teamPlayersPair)
            updateList()
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
        return true
    }
}

class TeamAdapter(private val context: Context, var allTeams: List<Pair<Team, Int>>): BaseAdapter() {

    override fun getCount(): Int {
        return allTeams.size
    }

    override fun getItem(position: Int): Pair<Team, Int> {
        return allTeams[position]
    }

    override fun getItemId(position: Int): Long {
        return allTeams[position].first.uid ?: -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.layout_list_view_row_item, parent, false)
        val teamListItem = getItem(position)
        val team = teamListItem.first
        val playersCount = teamListItem.second
        view.findViewById<TextView>(R.id.text_view_1).text = team.teamName
        view.findViewById<TextView>(R.id.text_view_2).text = context.resources.getQuantityString(R.plurals.numberOfPlayers, playersCount, playersCount)
        return view
    }
}