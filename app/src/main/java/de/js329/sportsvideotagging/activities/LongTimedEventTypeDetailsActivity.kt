package de.js329.sportsvideotagging.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.switchmaterial.SwitchMaterial
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ConfigurationController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.LongTimedEventType
import kotlinx.coroutines.launch

class LongTimedEventTypeDetailsActivity : AppCompatActivity() {

    private val configurationController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        ConfigurationController(db.eventDao(), db.playerDao(), db.teamDao())
    }

    private lateinit var longTimedEventTypeDetailsHeader: TextView
    private lateinit var toggleSwitchableSwitch: SwitchMaterial
    private lateinit var eventATitleEdittext: EditText
    private lateinit var eventBLabel: TextView
    private lateinit var eventBTitleEdittext: EditText
    private var longTimedEventType: LongTimedEventType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_long_timed_event_type_details)

        val eventTypeId = intent.getLongExtra("eventTypeId", -1)
        longTimedEventTypeDetailsHeader = findViewById(R.id.eventTypeEditHeader)
        toggleSwitchableSwitch = findViewById(R.id.toggleableSwitchableSwitch)
        eventATitleEdittext = findViewById(R.id.eventA_title_editText)
        eventBLabel = findViewById(R.id.eventB_editTextLabel)
        eventBTitleEdittext = findViewById(R.id.eventB_title_editText)
        findViewById<Button>(R.id.cancelBtn).setOnClickListener { this.finish() }
        findViewById<Button>(R.id.saveBtn).setOnClickListener { onSaveBtnClicked() }

        toggleSwitchableSwitch.setOnCheckedChangeListener { _, isChecked -> onSwitchChanged(isChecked) }

        setEventBVisibility(false)
        if (eventTypeId != -1L) {
            lifecycleScope.launch {
                longTimedEventType = configurationController.getLongTimedEventTypeForId(eventTypeId)
                longTimedEventType?.let {
                    setupLayout()
                }
            }
        } else {
            longTimedEventTypeDetailsHeader.text = getString(R.string.addLongTimedEventType)
        }
    }

    private fun setupLayout() {
        longTimedEventTypeDetailsHeader.text = getString(R.string.editEventTypesHeader_txt)

        longTimedEventType?.let {
            toggleSwitchableSwitch.isChecked = it.switchable
            eventATitleEdittext.setText(it.eventATitle)
            if (it.switchable) {
                eventBTitleEdittext.setText(it.eventBTitle)
            }
        }
    }

    private fun onSwitchChanged(isChecked: Boolean) {
        setEventBVisibility(isChecked)
    }

    private fun setEventBVisibility(visible: Boolean) {
        eventBLabel.isVisible = visible
        eventBTitleEdittext.isVisible = visible
    }

    private fun onSaveBtnClicked() {
        val eventATitle = eventATitleEdittext.text.toString()
        if (eventATitle == "") {
            Toast.makeText(this, R.string.noEventTypeTitle, Toast.LENGTH_LONG).show()
            return
        }
        val eventBTitle = if (toggleSwitchableSwitch.isChecked) eventBTitleEdittext.text.toString() else null
        if (eventBTitle == "") {
            Toast.makeText(this, R.string.noEventTypeTitle, Toast.LENGTH_LONG).show()
            return
        }
        val eventTypeActive = longTimedEventType?.activeEventType ?: true
        val longTimedEventType = LongTimedEventType(this.longTimedEventType?.uid, toggleSwitchableSwitch.isChecked, eventATitle, eventBTitle, eventTypeActive)
        lifecycleScope.launch {
            if (this@LongTimedEventTypeDetailsActivity.longTimedEventType != null) {
                configurationController.updateEventType(longTimedEventType)
            } else {
                configurationController.addLongTimedEventType(longTimedEventType)
            }
            this@LongTimedEventTypeDetailsActivity.finish()
        }
    }

}