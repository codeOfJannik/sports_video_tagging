package de.js329.sportsvideotagging.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.database.VideoTagDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val db by lazy { VideoTagDatabase.getInstance(applicationContext, lifecycleScope) }
    private var teamCount = 0
    private var eventTypeCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val startTaggingBtn = findViewById<Button>(R.id.startTaggingBtn)
        val configTaggingOptsBtn = findViewById<Button>(R.id.configTaggingOptionsBtn)
        val taggedMatchesOverviewBtn = findViewById<Button>(R.id.viewTaggedMatchesBtn)

        startTaggingBtn.setOnClickListener(onStartTaggingClicked)
        configTaggingOptsBtn.setOnClickListener {
            val intent = Intent(this, ConfigurationActivity::class.java)
            startActivity(intent)
        }
        taggedMatchesOverviewBtn.setOnClickListener {
            val intent = Intent(this, TaggedMatchesOverviewActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            teamCount = db.teamDao().getAll().size
            eventTypeCount = db.eventDao().getAllEventTypes().size
        }
    }

    private val onStartTaggingClicked = View.OnClickListener {

        if (teamCount < 2) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.noTeamsAlertTitle)
            builder.setMessage(R.string.noTeamsAlertMessage)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.show()
            return@OnClickListener
        }

        if (eventTypeCount < 2) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.noEventTypesAlertTitle)
            builder.setMessage(R.string.noEventTypesAlertMessage)
            builder.setPositiveButton(android.R.string.ok, null)
            builder.show()
            return@OnClickListener
        }

        val intent = Intent(this, PreTaggingActivity::class.java)
        startActivity(intent)
    }
}