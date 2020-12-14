package de.js329.sportsvideotagging.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.activities.TaggingFragmentManager
import de.js329.sportsvideotagging.adapter.EventTypesRecyclerAdapter
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.inflate
import de.js329.sportsvideotagging.toHHMMSSString
import kotlinx.coroutines.launch
import java.time.Duration
import kotlin.collections.ArrayList

class TaggingMatchBaseFragment : Fragment(), EventTypesRecyclerAdapter.ItemClickListener {

    private var eventTypes: List<EventType> = ArrayList()
    private lateinit var matchTaggingController: MatchTaggingController
    private lateinit var taggingFragmentManager: TaggingFragmentManager
    private var matchStarted = false
    private var taggingStartTimestamp = 0L
    private var homeTeamId = -1L
    private var awayTeamId = -1L
    private lateinit var tagCounter: TextView
    private lateinit var timerTextView: TextView
    private lateinit var mainHandler: Handler
    private lateinit var recyclerView: RecyclerView

    private val updateTimerTask = object : Runnable {
        override fun run() {
            updateTimerTextView()
            mainHandler.postDelayed(this, 1000)
        }
    }

    companion object {
        fun newInstance(
                matchTaggingController: MatchTaggingController,
                homeId: Long,
                awayId: Long,
                taggingFragmentManager: TaggingFragmentManager
        ) = TaggingMatchBaseFragment().apply {
            this.matchTaggingController = matchTaggingController
            this.homeTeamId = homeId
            this.awayTeamId = awayId
            this.taggingFragmentManager = taggingFragmentManager
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainHandler = Handler(Looper.getMainLooper())
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_match_tagging_base, container, false)
        timerTextView = view.findViewById(R.id.taggingTimerTextView)
        tagCounter =  view.findViewById(R.id.tagsCountTextView)

        setupRecyclerView(view)
        setupLayout(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        if (matchStarted) mainHandler.post(updateTimerTask)
    }

    override fun onPause() {
        super.onPause()
        mainHandler.removeCallbacks(updateTimerTask)
    }

    private fun setupLayout(view: View) {
        view.findViewById<Button>(R.id.matchRecordingBtn).apply {
            setOnClickListener { onRecordMatchTaggingClicked(it as Button) }
            val startIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_circle_outline_24)
            startIcon?.setTint(context.getColor(R.color.black))
            setCompoundDrawablesWithIntrinsicBounds(startIcon, null, null, null)
        }

        timerTextView.text = getString(R.string.noTimerStarted_TextViewValue)
        tagCounter.text = getString(R.string.tagCounterTextViewValue, 0)

        // TODO: Setup Undo button
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerView)
        lifecycleScope.launch {
            setEventTypes(matchTaggingController.getEventTypes())
            recyclerView.adapter = EventTypesRecyclerAdapter(this@TaggingMatchBaseFragment, eventTypes)
        }
        recyclerView.layoutManager = GridLayoutManager(activity, 2)
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.HORIZONTAL))
        recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))
        recyclerView.alpha = 0.5F
    }

    private fun updateTimerTextView() {
        val timestampDifference = Duration.ofSeconds(getTimeOffset())
        timerTextView.text = getString(R.string.timerTextViewTemplate, timestampDifference.toHHMMSSString())
    }


    private fun onRecordMatchTaggingClicked(button: Button) {
        if (!matchStarted) {
            taggingStartTimestamp = System.currentTimeMillis() / 1000
            mainHandler.post(updateTimerTask)
            matchStarted = true
            lifecycleScope.launch {
                matchTaggingController.startMatch(homeTeamId, awayTeamId, taggingStartTimestamp)
            }
            context?.let {
                button.text = it.getString(R.string.stopMatchTagging)
                button.setBackgroundColor(it.getColor(android.R.color.holo_red_dark))
                val stopIcon = ContextCompat.getDrawable(it, R.drawable.ic_baseline_stop_circle_24)
                stopIcon?.setTint(it.getColor(R.color.black))
                button.setCompoundDrawablesWithIntrinsicBounds(stopIcon, null, null, null)
            }
            recyclerView.alpha = 1F
        } else {
            // TODO: potential add Match End Event
            // TODO: make sure all data is saved to database
            // TODO: return to main activity
        }
    }

    private fun getTimeOffset(): Long {
        return if (taggingStartTimestamp != 0L) System.currentTimeMillis() / 1000 - taggingStartTimestamp else 0
    }

    private fun setEventTypes(eventTypes: List<EventType>) {
        val tempList = eventTypes.toMutableList()
        tempList.removeIf { it.eventTitle == "Match Start" }
        this.eventTypes = tempList
    }

    override fun onItemClick(view: View?, position: Int) {
        val eventType = eventTypes[position]
        matchTaggingController.createMatchEvent(eventType, System.currentTimeMillis() / 1000)
        if (eventType.playerSelection) {
            taggingFragmentManager.switchToPlayerSelection()
        }
        // TODO("Add match event logic including database operations and fragment transaction")
    }
}