package de.js329.sportsvideotagging.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.datamodels.Team
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val db by lazy { VideoTagDatabase.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startTaggingBtn = findViewById<Button>(R.id.startTaggingBtn)
        val configTaggingOptsBtn = findViewById<Button>(R.id.configTaggingOptionsBtn)

        startTaggingBtn.setOnClickListener(onStartTaggingClicked)
        configTaggingOptsBtn.setOnClickListener(onConfigureTagOptionsClicked)
    }

    private val onStartTaggingClicked = View.OnClickListener {
        var allTeams: List<Team> = ArrayList()
        var allEventTypes: List<EventType> = ArrayList()

        lifecycleScope.launch {
            val teamQueryResult = db.teamDao().getAll()
            val eventTypeQueryResult = db.eventDao().getAllEventTypes()
            allTeams = teamQueryResult
            allEventTypes = eventTypeQueryResult
        }

        if (allTeams.size < 2) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.noTeamsAlertTitle)
            builder.setMessage(R.string.noTeamsAlertMessage)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.show()
            return@OnClickListener
        }

        if (allEventTypes.isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.noEventTypesAlertTitle)
            builder.setMessage(R.string.noEventTypesAlertMessage)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.show()
            return@OnClickListener
        }

        val intent = Intent(this, ConfigurationActivity::class.java)
        startActivity(intent)
    }

    private val onConfigureTagOptionsClicked = View.OnClickListener {
        val intent = Intent(this, ConfigurationActivity::class.java)
        startActivity(intent)
    }
}