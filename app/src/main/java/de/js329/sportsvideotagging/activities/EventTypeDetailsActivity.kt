package de.js329.sportsvideotagging.activities

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.controller.ConfigurationController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.EventType
import kotlinx.coroutines.launch

class EventTypeDetailsActivity : AppCompatActivity() {

    private val configurationController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        ConfigurationController(db.eventDao(), db.playerDao(), db.teamDao())
    }

    private lateinit var eventTypeTitleEditText: EditText
    private lateinit var timeOffsetPreSign: RadioGroup
    private lateinit var timeOffsetSecondsEditText: EditText
    private lateinit var longTimedEventCheckBox: CheckBox
    private lateinit var enablePlayerSelectionCheckBox: CheckBox
    private lateinit var attributesAllowedCheckBox: CheckBox
    private var eventType: EventType? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_type_details)

        val eventTypeId = intent.getLongExtra("eventTypeId", -1)
        eventTypeTitleEditText = findViewById(R.id.eventTypeTitleEditText)
        timeOffsetPreSign = findViewById(R.id.positiveNegativeTimeOffsetRadioGroup)
        timeOffsetSecondsEditText = findViewById(R.id.timeOffsetSecondsEditText)
        longTimedEventCheckBox = findViewById(R.id.longTimeEventCheckBox)
        enablePlayerSelectionCheckBox = findViewById(R.id.playerSelectionEnabledCheckBox)
        attributesAllowedCheckBox = findViewById(R.id.allowAttributesCheckBox)
        findViewById<Button>(R.id.cancelEventTypeBtn).setOnClickListener { this.finish() }
        findViewById<Button>(R.id.saveEventTypeBtn).setOnClickListener { onSaveClicked() }

        if (eventTypeId > -1) {
            lifecycleScope.launch {
                eventType = configurationController.getEventTypeForId(eventTypeId)
                eventType?.let {
                    setupLayout()
                }
            }
        } else {
            findViewById<TextView>(R.id.eventTypeEditHeader).text = getString(R.string.addEventType)
            timeOffsetPreSign.check(R.id.positiveTimeOfffset)
        }
    }

    private fun setupLayout() {
        findViewById<TextView>(R.id.eventTypeEditHeader).text = getString(R.string.editEventTypesHeader_txt)

        eventType?.let {
            eventTypeTitleEditText.setText(it.eventTitle)
            if (it.timeOffset >= 0) timeOffsetPreSign.check(R.id.positiveTimeOfffset) else timeOffsetPreSign.check(R.id.negativeTimeOffset)
            timeOffsetSecondsEditText.setText(it.timeOffset.toString())
            longTimedEventCheckBox.isChecked = it.longTimedEvent
            enablePlayerSelectionCheckBox.isChecked = it.playerSelection
            attributesAllowedCheckBox.isChecked = it.attributesAllowed
        }
    }

    private fun onSaveClicked() {
        val titleInput = eventTypeTitleEditText.text.toString()
        if (titleInput == "") {
            Toast.makeText(this, R.string.noEventTypeTitle, Toast.LENGTH_LONG).show()
            return
        }
        var timeOffset = if (timeOffsetSecondsEditText.text.toString() != "") timeOffsetSecondsEditText.text.toString().toLong() else 0
        timeOffset = if (findViewById<RadioButton>(R.id.negativeTimeOffset).isChecked) timeOffset * (-1) else timeOffset
        val longTimed = longTimedEventCheckBox.isChecked
        val playerSelectionEnabled = enablePlayerSelectionCheckBox.isChecked
        val attributesAllowed = attributesAllowedCheckBox.isChecked
        val isActive = eventType?.activeEventType ?: true
        val updatedEventType = EventType(
                eventType?.uid,
                titleInput,
                longTimed,
                timeOffset,
                playerSelectionEnabled,
                attributesAllowed,
                isActive
        )
        lifecycleScope.launch {
            if (eventType != null) {
                configurationController.updateEventType(updatedEventType)
            } else {
                configurationController.addEventType(updatedEventType)
            }
            this@EventTypeDetailsActivity.finish()
        }
    }
}