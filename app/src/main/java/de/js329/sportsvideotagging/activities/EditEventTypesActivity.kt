package de.js329.sportsvideotagging.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.adapter.EventTypesAdapter
import de.js329.sportsvideotagging.controller.ConfigurationController
import de.js329.sportsvideotagging.database.VideoTagDatabase
import de.js329.sportsvideotagging.datamodels.EventType
import kotlinx.coroutines.launch

interface EventTypeChangedListener {
    fun onEventTypeActiveChanged(eventType: EventType, isActive: Boolean)
}


class EditEventTypesActivity : AppCompatActivity(), EventTypeChangedListener {

    private val configurationController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        ConfigurationController(db.eventDao(), db.playerDao(), db.teamDao())
    }

    private var eventTypes: MutableList<EventType> = ArrayList()
    private val eventTypesAdapter by lazy { EventTypesAdapter(this, eventTypes,this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration_listview)
        findViewById<TextView>(R.id.listViewHeader).text = getString(R.string.editEventTypesHeader_txt)
        findViewById<FloatingActionButton>(R.id.addFAB).setOnClickListener { onAddClicked() }
        val listView = findViewById<ListView>(R.id.listView)
        listView.itemsCanFocus = true
        listView.setOnItemLongClickListener { _, _, position, _ ->
            onLongItemClicked(position)
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            onItemClicked(position)
        }
        listView.adapter = eventTypesAdapter
    }

    override fun onStart() {
        super.onStart()
        queryEventTypes()
    }

    private fun queryEventTypes() {
        lifecycleScope.launch {
            eventTypes = configurationController.getAllEventTypes().toMutableList()
            eventTypes.remove(eventTypes.first { it.eventTitle == "Match Start" })
            updateList()
        }
    }

    private fun updateList() {
        eventTypesAdapter.allEventTypes = eventTypes
        eventTypesAdapter.notifyDataSetChanged()
    }

    private fun onAddClicked() {
        val intent = Intent(this, EventTypeDetailsActivity::class.java)
        startActivity(intent)
    }

    private fun onItemClicked(position: Int) {
        val intent = Intent(this, EventTypeDetailsActivity::class.java)
        intent.putExtra("eventTypeId", eventTypesAdapter.getItem(position).uid)
        startActivity(intent)
    }

    private fun onLongItemClicked(position: Int): Boolean {
        val eventType = eventTypesAdapter.getItem(position)

        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.deleteEventTypeTitle_txt)
        builder.setMessage(getString(R.string.deleteEventTypeMessage_txt, eventType.eventTitle))
        builder.setPositiveButton(android.R.string.ok) { _, _ ->
            lifecycleScope.launch {configurationController.deleteEventType(eventType)}
            eventTypes.remove(eventType)
            updateList()
        }

        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
        return true
    }

    override fun onEventTypeActiveChanged(eventType: EventType, isActive: Boolean) {
        val updatedEventType = EventType(
            eventType.uid,
            eventType.eventTitle,
            eventType.longTimedEvent,
            eventType.timeOffset,
            eventType.playerSelection,
            eventType.attributesAllowed,
            isActive
        )
        lifecycleScope.launch {
            configurationController.updateEventType(updatedEventType)
            val index = eventTypes.indexOf(eventType)
            eventTypes[index] = updatedEventType
            updateList()
        }
    }
}