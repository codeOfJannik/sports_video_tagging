package de.js329.sportsvideotagging.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.Team
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PreTaggingActivity : AppCompatActivity() {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val matchDateCalendar = Calendar.getInstance()
    private val matchTaggingController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        MatchTaggingController(db.matchDao(), db.eventDao(), db.eventJoinDao(), db.teamDao(), db.playerDao())
    }
    private var allTeams: List<Team> = ArrayList()
    private var selectedHomeTeam: Team? = null
    private var selectedguestTeam: Team? = null

    private lateinit var matchDateEditText: EditText
    private lateinit var matchTimeEditText: EditText
    private lateinit var homeTeamEditText: AutoCompleteTextView
    private lateinit var guestTeamEditText: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_tagging)

        findViewById<Button>(R.id.cancelBtn).setOnClickListener { this.finish() }
        findViewById<Button>(R.id.continueBtn).setOnClickListener { onContinueClicked() }
        findViewById<Button>(R.id.editHomeTeamPlayersBtn).setOnClickListener { onEditTeamPlayersClicked(selectedHomeTeam) }
        findViewById<Button>(R.id.editguestTeamPlayersBtn).setOnClickListener { onEditTeamPlayersClicked(selectedguestTeam) }
        matchDateEditText = findViewById(R.id.matchDateEditText)
        matchTimeEditText = findViewById(R.id.matchTimeEditText)
        homeTeamEditText = findViewById(R.id.homeTeamEditText)
        guestTeamEditText = findViewById(R.id.guestTeamEditText)

        lifecycleScope.launch {
            allTeams = matchTaggingController.getAllTeams()
            val teamNames = ArrayList<String>()
            allTeams.map { teamNames.add(it.teamName) }
            selectedHomeTeam = allTeams[0]
            selectedguestTeam = allTeams[1]
            setupLayout(teamNames)
        }
    }

    private fun setupLayout(teamNames: ArrayList<String>) {
        updateDateEditTextValues()
        homeTeamEditText.setText(selectedHomeTeam?.teamName)
        guestTeamEditText.setText(selectedguestTeam?.teamName)

        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, teamNames)
        homeTeamEditText.setAdapter(arrayAdapter)
        homeTeamEditText.setOnClickListener { homeTeamEditText.showDropDown() }
        homeTeamEditText.setOnItemClickListener { _, _, position, _ ->
            arrayAdapter.getItem(position)?.let {
                lifecycleScope.launch {
                    val team = getTeamForName(it)
                    selectedHomeTeam = team
                }
            }
        }
        guestTeamEditText.setAdapter(arrayAdapter)
        guestTeamEditText.setOnClickListener { guestTeamEditText.showDropDown() }
        guestTeamEditText.setOnItemClickListener { _, _, position, _ ->
            arrayAdapter.getItem(position)?.let {
                lifecycleScope.launch {
                    val team = getTeamForName(it)
                    selectedguestTeam = team
                }
            }
        }

        matchDateEditText.setOnClickListener { onEditDateClicked() }
        matchTimeEditText.setOnClickListener { onEditTimeClicked() }
    }

    private fun onEditDateClicked() {
        val onSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            matchDateCalendar.set(Calendar.YEAR, year)
            matchDateCalendar.set(Calendar.MONTH, month)
            matchDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateEditTextValues()
        }
        DatePickerDialog(
                this,
                onSetListener,
                matchDateCalendar.get(Calendar.YEAR),
                matchDateCalendar.get(Calendar.MONTH),
                matchDateCalendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun onEditTimeClicked() {
        val onSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            matchDateCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            matchDateCalendar.set(Calendar.MINUTE, minute)
            matchDateCalendar.set(Calendar.SECOND, 0)
            updateDateEditTextValues()
        }
        TimePickerDialog(
                this,
                onSetListener,
                matchDateCalendar.get(Calendar.HOUR_OF_DAY),
                matchDateCalendar.get(Calendar.MINUTE),
                true
        ).show()
    }

    private suspend fun getTeamForName(teamName: String): Team {
        return matchTaggingController.getTeamForName(teamName)
    }

    private fun updateDateEditTextValues() {
        matchDateEditText.setText(dateFormatter.format(matchDateCalendar.time))
        matchTimeEditText.setText(timeFormatter.format(matchDateCalendar.time))
    }

    private fun onContinueClicked() {
        if (selectedHomeTeam != selectedguestTeam) {
            val intent = Intent(this, TaggingActivity::class.java)
            intent.putExtra("homeTeamId", selectedHomeTeam?.uid)
            intent.putExtra("guestTeamId", selectedguestTeam?.uid)
            intent.putExtra("matchDate", matchDateCalendar.time.time)
            startActivity(intent)
        } else {
            Toast.makeText(this, R.string.sameTeamError, Toast.LENGTH_LONG).show()
        }
    }

    private fun onEditTeamPlayersClicked(team: Team?) {
        team?.let {
            val intent = Intent(this, EditPlayersActivity::class.java)
            intent.putExtra("teamId", team.uid)
            startActivity(intent)
        }
    }
}