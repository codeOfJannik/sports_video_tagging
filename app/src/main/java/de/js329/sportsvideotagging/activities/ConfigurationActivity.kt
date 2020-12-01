package de.js329.sportsvideotagging.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import de.js329.sportsvideotagging.R

class ConfigurationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)

        val editTeamsBtn = findViewById<Button>(R.id.editTeamsBtn)
        val editEventTypesBtn = findViewById<Button>(R.id.editEventTypesBtn)
        val editAttributesBtn = findViewById<Button>(R.id.editAttributesBtn)

        editTeamsBtn.setOnClickListener(editTeamsCLicked)
        editAttributesBtn.setOnClickListener(editAttributesClicked)
    }

    private val editTeamsCLicked = View.OnClickListener {
        val intent = Intent(this, EditTeamsActivity::class.java)
        startActivity(intent)
    }

    private val editAttributesClicked = View.OnClickListener {
        val intent = Intent(this, EditAttributesActivity::class.java)
        startActivity(intent)
    }
}