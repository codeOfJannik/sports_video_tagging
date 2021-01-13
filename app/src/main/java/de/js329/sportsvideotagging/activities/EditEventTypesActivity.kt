package de.js329.sportsvideotagging.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
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
import de.js329.sportsvideotagging.datamodels.LongTimedEventType
import kotlinx.coroutines.launch

interface EventTypeChangedListener {
    fun onEventTypeActiveChanged(eventType: Any, isActive: Boolean)
}


class EditEventTypesActivity : AppCompatActivity(), EventTypeChangedListener {

    private val configurationController by lazy {
        val db = VideoTagDatabase.getInstance(this, lifecycleScope)
        ConfigurationController(db.eventDao(), db.playerDao(), db.teamDao())
    }

    private lateinit var addEventTypeLayoutButton: LinearLayout
    private lateinit var addLongTimedEventTypeLayoutButton: LinearLayout
    private lateinit var addFab: FloatingActionButton

    private var fabExpanded = false
    private var longTimedEventTypes: MutableList<LongTimedEventType> = ArrayList()
    private var eventTypes: MutableList<EventType> = ArrayList()
    private val eventTypesAdapter by lazy { EventTypesAdapter(this, eventTypes, longTimedEventTypes, this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration_listview)
        val fabFrame = findViewById<FrameLayout>(R.id.fabFrame)
        addEventTypeLayoutButton = fabFrame.findViewById(R.id.layoutFabAddEventType)
        addLongTimedEventTypeLayoutButton = fabFrame.findViewById(R.id.layoutFabAddLongTimedEventType)
        addFab = findViewById(R.id.addFAB)
        findViewById<TextView>(R.id.listViewHeader).text = getString(R.string.editEventTypesHeader_txt)
        addFab.setOnClickListener { onAddClicked() }
        findViewById<FloatingActionButton>(R.id.addEventTypeBtn).setOnClickListener { onAddEventTypeClicked() }
        findViewById<FloatingActionButton>(R.id.addLongEventTypeBtn).setOnClickListener { onAddLongTimedEventTypeClicked() }
        val listView = findViewById<ListView>(R.id.listView)
        listView.itemsCanFocus = true
        listView.setOnItemLongClickListener { _, _, position, _ ->
            onLongItemClicked(position)
        }
        listView.setOnItemClickListener { _, _, position, _ ->
            onItemClicked(position)
        }
        listView.adapter = eventTypesAdapter
        closeSubMenusFab()
    }

    override fun onStart() {
        super.onStart()
        queryEventTypes()
    }

    private fun queryEventTypes() {
        lifecycleScope.launch {
            eventTypes = configurationController.getAllEventTypes().toMutableList()
            longTimedEventTypes = configurationController.getAllLongTimedEventTypes().toMutableList()
            eventTypes.remove(eventTypes.first { it.eventTitle == "Match Start" })
            updateList()
        }
    }

    private fun updateList() {
        eventTypesAdapter.allEventTypes = eventTypes
        eventTypesAdapter.allLongTimedEventTypes = longTimedEventTypes
        eventTypesAdapter.notifyDataSetChanged()
    }

    private fun onAddClicked() {
        if (fabExpanded) {
            closeSubMenusFab()
        } else {
            openSubMenuFab()
        }
    }

    private fun onAddEventTypeClicked() {
        val intent = Intent(this, EventTypeDetailsActivity::class.java)
        startActivity(intent)
    }

    private fun onAddLongTimedEventTypeClicked() {
        val intent = Intent(this, LongTimedEventTypeDetailsActivity::class.java)
        startActivity(intent)
    }

    private fun onItemClicked(position: Int) {
        val intent: Intent
        when (val item = eventTypesAdapter.getItem(position)) {
            is String -> return
            is EventType -> {
                intent = Intent(this, EventTypeDetailsActivity::class.java)
                intent.putExtra("eventTypeId", item.uid)
            }
            is LongTimedEventType -> {
                intent = Intent(this, LongTimedEventTypeDetailsActivity::class.java)
                intent.putExtra("eventTypeId", item.uid)
            }
            else -> return
        }
        startActivity(intent)
    }

    private fun onLongItemClicked(position: Int): Boolean {
        val builder = AlertDialog.Builder(this)
        when (val item = eventTypesAdapter.getItem(position)) {
            is String -> return true
            is EventType -> {
                builder.setMessage(getString(R.string.deleteEventTypeMessage_txt, item.eventTitle))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    lifecycleScope.launch { configurationController.deleteEventType(item) }
                    eventTypes.remove(item)
                    updateList()
                }
            }
            is LongTimedEventType -> {
                builder.setMessage(getString(R.string.deleteEventTypeMessage_txt, item.eventATitle))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    lifecycleScope.launch { configurationController.deleteEventType(item) }
                    longTimedEventTypes.remove(item)
                    updateList()
                }
            }
        }
        builder.setTitle(R.string.deleteEventTypeTitle_txt)
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
        return true
    }

    private fun closeSubMenusFab() {
        addEventTypeLayoutButton.visibility = View.GONE
        addLongTimedEventTypeLayoutButton.visibility = View.GONE
        addFab.setImageResource(R.drawable.ic_baseline_add_24)
        fabExpanded = false
    }

    private fun openSubMenuFab() {
        addEventTypeLayoutButton.visibility = View.VISIBLE
        addLongTimedEventTypeLayoutButton.visibility = View.VISIBLE
        addFab.setImageResource(R.drawable.ic_baseline_close_24)
        fabExpanded = true
    }

    override fun onEventTypeActiveChanged(eventType: Any, isActive: Boolean) {
        when (eventType) {
            is EventType -> {
                val updatedEventType = EventType(
                        eventType.uid,
                        eventType.eventTitle,
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
            is LongTimedEventType -> {
                val updated = LongTimedEventType(
                        eventType.uid,
                        eventType.switchable,
                        eventType.eventATitle,
                        eventType.eventBTitle,
                        isActive
                )
                lifecycleScope.launch {
                    configurationController.updateEventType(updated)
                    val index = longTimedEventTypes.indexOf(eventType)
                    longTimedEventTypes[index] = updated
                    updateList()
                }
            }
        }
    }
}