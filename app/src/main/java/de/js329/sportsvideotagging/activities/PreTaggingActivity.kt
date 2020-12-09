package de.js329.sportsvideotagging.activities

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.Team
import kotlinx.android.synthetic.main.activity_tagging.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.zip.Inflater
import kotlin.collections.ArrayList

class PreTaggingActivity : AppCompatActivity() {

    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val matchDateCalendar = Calendar.getInstance()
    private val matchTaggingController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        MatchTaggingController(db.matchDao(), db.eventDao(), db.eventJoinDao(), db.teamDao())
    }
    private var allTeams: List<Team> = ArrayList()
    private var selectedHomeTeam: Team? = null
    private var selectedAwayTeam: Team? = null

    private lateinit var matchDateEditText: EditText
    private lateinit var matchTimeEditText: EditText
    private lateinit var homeTeamEditText: AutoCompleteTextView
    private lateinit var awayTeamEditText: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_tagging)

        findViewById<Button>(R.id.cancelBtn).setOnClickListener { this.finish() }
        findViewById<Button>(R.id.continueBtn).setOnClickListener { onContinueClicked() }
        findViewById<Button>(R.id.editHomeTeamPlayersBtn).setOnClickListener { onEditTeamPlayersClicked(selectedHomeTeam) }
        findViewById<Button>(R.id.editAwayTeamPlayersBtn).setOnClickListener { onEditTeamPlayersClicked(selectedAwayTeam) }
        matchDateEditText = findViewById(R.id.matchDateEditText)
        matchTimeEditText = findViewById(R.id.matchTimeEditText)
        homeTeamEditText = findViewById(R.id.homeTeamEditText)
        awayTeamEditText = findViewById(R.id.awayTeamEditText)

        lifecycleScope.launch {
            allTeams = matchTaggingController.getAllTeams()
            val teamNames = ArrayList<String>()
            allTeams.map { teamNames.add(it.teamName) }
            selectedHomeTeam = allTeams[0]
            selectedAwayTeam = allTeams[1]
            setupLayout(teamNames)
        }
    }

    private fun setupLayout(teamNames: ArrayList<String>) {
        updateDateEditTextValues()
        homeTeamEditText.setText(selectedHomeTeam?.teamName)
        awayTeamEditText.setText(selectedAwayTeam?.teamName)

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
        awayTeamEditText.setAdapter(arrayAdapter)
        awayTeamEditText.setOnClickListener { awayTeamEditText.showDropDown() }
        awayTeamEditText.setOnItemClickListener { _, _, position, _ ->
            arrayAdapter.getItem(position)?.let {
                lifecycleScope.launch {
                    val team = getTeamForName(it)
                    selectedAwayTeam = team
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
        val intent = Intent(this, TaggingActivity::class.java)
        startActivity(intent)
    }

    private fun onEditTeamPlayersClicked(team: Team?) {
        team?.let {
            val intent = Intent(this, EditPlayersActivity::class.java)
            intent.putExtra("teamId", team.uid)
            startActivity(intent)
        }
    }
}