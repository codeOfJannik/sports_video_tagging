package de.js329.sportsvideotagging.activities

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ConfigurationController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.Player
import de.js329.sportsvideotagging.datamodels.Team
import kotlinx.coroutines.launch

class EditPlayersActivity : AppCompatActivity() {

    private val configurationController by lazy {
        val db = VideoTagDatabase.getInstance(this)
        ConfigurationController(db.eventDao(), db.playerDao(), db.teamDao())
    }

    private val teamId by lazy { intent.getLongExtra("teamId", -1) }
    private var team: Team? = null
    private var players: MutableList<Player> = ArrayList()
    private val playerAdapter by lazy { PlayerAdapter(this, players) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration_listview)


        val listView = findViewById<ListView>(R.id.listView)
        val addPlayerFAB = findViewById<FloatingActionButton>(R.id.addFAB)
        addPlayerFAB.setOnClickListener {
            onAddPlayerClicked()
        }
        listView.setOnItemLongClickListener { _, _, position, _ ->
            onItemLongClick(position)
        }
        listView.adapter = playerAdapter

        queryTeamAndPlayers()
    }

    @SuppressLint("InflateParams")
    private fun onAddPlayerClicked() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.addPlayerTitle)
        builder.setMessage(R.string.addPlayerMessage)
        val contentView = LayoutInflater.from(this).inflate(R.layout.layout_add_player_dialog, null)
        val playerNameInput = contentView.findViewById<EditText>(R.id.playerNameEditText)
        val playerNumberInput = contentView.findViewById<EditText>(R.id.playerNumberEditText)
        contentView.findViewById<TextView>(R.id.playerTeamContentTextView).text = team?.teamName
        builder.setView(contentView)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            val playerName = playerNameInput?.text.toString()
            val playerNumber = playerNumberInput?.text.toString()

            if (playerNumber == "") {
                Toast.makeText(this, R.string.playerNumberEmpty, Toast.LENGTH_LONG).show()
                return@setPositiveButton
            }

            lifecycleScope.launch {
                val newPlayer = configurationController.addPlayer(playerName, playerNumber.toInt(), teamId)
                newPlayer?.let {
                    players.add(newPlayer)
                    updateList()
                } ?: kotlin.run {
                    Toast.makeText(this@EditPlayersActivity, R.string.playerAlreadyExists, Toast.LENGTH_LONG).show()
                }
            }
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.create().show()
    }

    private fun onItemLongClick(position: Int): Boolean {
        val player = playerAdapter.getItem(position)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.deletePlayerTitle_txt)
        builder.setMessage(R.string.deletePlayerMessage_txt)
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            lifecycleScope.launch {configurationController.deletePlayer(player)}
            players.remove(player)
            updateList()
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
        return true
    }

    private fun queryTeamAndPlayers() {
        players.clear()
        lifecycleScope.launch {
            team = configurationController.getTeamForId(teamId)
            val queriedPlayers = configurationController.getPlayersForTeam(teamId)
            players = queriedPlayers.toMutableList()
            updateList()
            findViewById<TextView>(R.id.listViewHeader).text = team?.teamName
        }

    }

    private fun updateList() {
        playerAdapter.players = players
        playerAdapter.notifyDataSetChanged()
    }
}

class PlayerAdapter(private val context: Context, var players: List<Player>): BaseAdapter() {

    override fun getCount(): Int {
        return players.size
    }

    override fun getItem(position: Int): Player {
        return players[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).playerId ?: -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.layout_player_list_row_item, parent, false)
        val listItem = getItem(position)
        view.findViewById<TextView>(R.id.playerNumberTextView).text = listItem.number.toString()
        view.findViewById<TextView>(R.id.playerNameTextView).text = listItem.name
        return view
    }

}