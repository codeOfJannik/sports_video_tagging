package de.js329.sportsvideotagging.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.activities.TaggingFragmentManager
import de.js329.sportsvideotagging.adapter.EventTypesRecyclerAdapter
import de.js329.sportsvideotagging.adapter.LongTimedEventTypesAdapter
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.toHHMMSSString
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList

class TaggingMatchBaseFragment : Fragment(), EventTypesRecyclerAdapter.ItemClickListener, LongTimedEventTypesAdapter.ItemClickListener {

    private var eventTypes: List<EventType> = ArrayList()
    private lateinit var longTimedEventAdapter: LongTimedEventTypesAdapter
    private lateinit var matchTaggingController: MatchTaggingController
    private lateinit var taggingFragmentManager: TaggingFragmentManager
    private var selectedLongTimedItemsIndeces: MutableList<Int> = ArrayList()
    private var matchStarted = false
    private var taggingStartTimestamp = 0L
    private var homeTeamId = -1L
    private var awayTeamId = -1L
    private var matchDate = Calendar.getInstance()
    private lateinit var tagCounter: TextView
    private lateinit var timerTextView: TextView
    private lateinit var mainHandler: Handler
    private lateinit var eventTypesRecyclerView: RecyclerView
    private lateinit var longTimedEventsRecyclerView: RecyclerView
    private lateinit var undoButton: Button

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
                matchDate: Long,
                taggingFragmentManager: TaggingFragmentManager
        ) = TaggingMatchBaseFragment().apply {
            this.matchTaggingController = matchTaggingController
            this.homeTeamId = homeId
            this.awayTeamId = awayId
            this.matchDate.time = Date(matchDate)
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
        tagCounter = view.findViewById(R.id.tagsCountTextView)
        undoButton = view.findViewById(R.id.undoBtn)

        setupLongTimesEventsRecyclerview(view)
        setupEventTypeRecyclerView(view)
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
            val startIcon: Drawable?
            if (!matchStarted) {
                text = context.getString(R.string.startTaggingBtn_txt)
                setBackgroundColor(context.getColor(R.color.green))
                setOnClickListener { onRecordMatchTaggingClicked(it as Button) }
                startIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_play_circle_outline_24)
                startIcon?.setTint(context.getColor(R.color.black))
            } else {
                text = context.getString(R.string.stopMatchTagging)
                setBackgroundColor(context.getColor(android.R.color.holo_red_dark))
                setOnClickListener { onMatchTaggingStopClicked() }
                startIcon = ContextCompat.getDrawable(context, R.drawable.ic_baseline_stop_circle_24)
                startIcon?.setTint(context.getColor(R.color.black))
            }
            setCompoundDrawablesWithIntrinsicBounds(startIcon, null, null, null)
        }

        timerTextView.text = getString(R.string.noTimerStarted_TextViewValue)
        tagCounter.text = getString(R.string.tagCounterTextViewValue, matchTaggingController.eventOrderNum)

        undoButton.isVisible = matchTaggingController.eventOrderNum > 0
        undoButton.setOnClickListener { onUndoButtonClicked() }
    }

    private fun setupEventTypeRecyclerView(view: View) {
        eventTypesRecyclerView = view.findViewById(R.id.eventTypesRecyclerView)
        lifecycleScope.launch {
            setEventTypes(matchTaggingController.getEventTypes())
            eventTypesRecyclerView.adapter = EventTypesRecyclerAdapter(this@TaggingMatchBaseFragment, eventTypes)
        }
        eventTypesRecyclerView.layoutManager = GridLayoutManager(activity, 2)
        eventTypesRecyclerView.setHasFixedSize(true)
        eventTypesRecyclerView.isNestedScrollingEnabled = false
        eventTypesRecyclerView.addItemDecoration(DividerItemDecoration(eventTypesRecyclerView.context, DividerItemDecoration.HORIZONTAL))
        eventTypesRecyclerView.addItemDecoration(DividerItemDecoration(eventTypesRecyclerView.context, DividerItemDecoration.VERTICAL))

        if (!matchStarted) eventTypesRecyclerView.alpha = 0.5F else eventTypesRecyclerView.alpha = 1F
    }

    private fun setupLongTimesEventsRecyclerview(view: View) {
        val layoutManager = GridLayoutManager(activity, 2)
        longTimedEventsRecyclerView = view.findViewById(R.id.longTimedEventsRecyclerView)
        lifecycleScope.launch {
            val longTimedEventTypes = matchTaggingController.getLongTimedEventTypes().toMutableList()
            longTimedEventTypes.removeIf { !it.activeEventType }
            longTimedEventAdapter = LongTimedEventTypesAdapter(this@TaggingMatchBaseFragment, longTimedEventTypes, ArrayList(selectedLongTimedItemsIndeces), requireContext())
            longTimedEventsRecyclerView.adapter = longTimedEventAdapter
            layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (longTimedEventAdapter.isSwitchableItem(position)) 1 else 2
                }
            }
        }
        longTimedEventsRecyclerView.layoutManager = layoutManager
        longTimedEventsRecyclerView.setHasFixedSize(true)
        longTimedEventsRecyclerView.isNestedScrollingEnabled = false
        longTimedEventsRecyclerView.addItemDecoration(DividerItemDecoration(longTimedEventsRecyclerView.context, DividerItemDecoration.VERTICAL))

        if (!matchStarted) longTimedEventsRecyclerView.alpha = 0.5F else longTimedEventsRecyclerView.alpha = 1F
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
            taggingFragmentManager.matchTaggingStarted()
            lifecycleScope.launch {
                matchTaggingController.startMatch(homeTeamId, awayTeamId, matchDate.time.time, taggingStartTimestamp)
            }
            context?.let {
                button.text = it.getString(R.string.stopMatchTagging)
                button.setBackgroundColor(it.getColor(android.R.color.holo_red_dark))
                val stopIcon = ContextCompat.getDrawable(it, R.drawable.ic_baseline_stop_circle_24)
                stopIcon?.setTint(it.getColor(R.color.black))
                button.setCompoundDrawablesWithIntrinsicBounds(stopIcon, null, null, null)
            }
            eventTypesRecyclerView.alpha = 1F
            longTimedEventsRecyclerView.alpha = 1F
        }
    }

    private fun onUndoButtonClicked() {
        lifecycleScope.launch {
            matchTaggingController.deleteLastMatchEvent()
            tagCounter.text = getString(R.string.tagCounterTextViewValue, matchTaggingController.eventOrderNum)
            undoButton.isVisible = matchTaggingController.eventOrderNum > 0
        }
    }

    @SuppressLint("InflateParams")
    fun onMatchTaggingStopClicked() {
        val builder = AlertDialog.Builder(requireContext())
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.layout_final_score_dialog, null)
        view.findViewById<TextView>(R.id.homeTeamTextView).text =
                requireContext().getString(R.string.teamPlaceholderScore_txt, matchTaggingController.homeTeam.teamName)
        view.findViewById<TextView>(R.id.awayTeamTextView).text =
                requireContext().getString(R.string.teamPlaceholderScore_txt, matchTaggingController.awayTeam.teamName)
        builder
                .setTitle(R.string.finalScoreDialogTitle_txt)
                .setMessage(R.string.finalScoreDialogMessage_txt)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    try {
                        val homeScore = view.findViewById<EditText>(R.id.homeTeamScore).text.toString().toInt()
                        val awayScore = view.findViewById<EditText>(R.id.awayTeamScore).text.toString().toInt()
                        onMatchTaggingFinished(homeScore, awayScore)
                    } catch (exception: NumberFormatException) {
                        Toast.makeText(requireContext(), R.string.invalidScoreMessage, Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton(android.R.string.cancel) {
                    dialog, _ -> dialog.dismiss()
                }
                .show()

    }

    private fun onMatchTaggingFinished(homeScore: Int, awayScore: Int) {
        lifecycleScope.launch {
            matchTaggingController.endMatch(homeScore, awayScore)
        }
        taggingFragmentManager.onTaggingFinishedCloseFragments()
    }

    private fun getTimeOffset(): Long {
        return if (taggingStartTimestamp != 0L) System.currentTimeMillis() / 1000 - taggingStartTimestamp else 0
    }

    private fun setEventTypes(eventTypes: List<EventType>) {
        val tempList = eventTypes.toMutableList()
        tempList.removeIf { it.eventTitle == "Match Start" }
        tempList.removeIf { !it.activeEventType }
        this.eventTypes = tempList
    }

    override fun onItemClick(view: View?, position: Int) {
        if (matchStarted) {
            val eventType = eventTypes[position]
            matchTaggingController.createMatchEvent(eventType, System.currentTimeMillis() / 1000)
            if (eventType.playerSelection) {
                taggingFragmentManager.switchToPlayerSelection()
                return
            }
            taggingFragmentManager.switchToFinalizeMatchInput()
        }
    }

    override fun onSwitchableItemClicked(view: View?, position: Int) {
        if (matchStarted) {
            if (longTimedEventAdapter.selectedItemIndices.contains(position)) {
                selectedLongTimedItemsIndeces = longTimedEventAdapter.selectedItemIndices
                return
            }
            val longTimedEvent = longTimedEventAdapter.getItem(position)
            val selectedTitle = longTimedEventAdapter.getStringItem(position)
            longTimedEvent?.let {
                var isSwitched = false
                longTimedEventAdapter.selectedItemIndices.add(position)
                if (selectedTitle == it.first.eventATitle) {
                    longTimedEventAdapter.selectedItemIndices.remove(position + 1)
                    isSwitched = false
                    longTimedEventAdapter.notifyItemRangeChanged(position, 2)
                }
                if (selectedTitle == it.first.eventBTitle) {
                    longTimedEventAdapter.selectedItemIndices.remove(position - 1)
                    isSwitched = true
                    longTimedEventAdapter.notifyItemRangeChanged(position - 1, 2)
                }
                lifecycleScope.launch {
                    matchTaggingController.createLongTimedMatchEvent(it.first, System.currentTimeMillis() / 1000, isSwitched)
                }
                selectedLongTimedItemsIndeces = longTimedEventAdapter.selectedItemIndices
            }
        }
    }

    override fun onToggleableItemClicked(view: View?, position: Int) {
        if (matchStarted) {
            val longTimedEvent = longTimedEventAdapter.getItem(position)
            longTimedEvent?.let {
                lifecycleScope.launch {
                    matchTaggingController.createLongTimedMatchEvent(it.first, System.currentTimeMillis() / 1000, false)
                }
            }
            if (longTimedEventAdapter.selectedItemIndices.contains(position)) {
                longTimedEventAdapter.selectedItemIndices.remove(position)
            } else {
                longTimedEventAdapter.selectedItemIndices.add(position)
            }
            longTimedEventAdapter.notifyItemChanged(position)
            selectedLongTimedItemsIndeces = longTimedEventAdapter.selectedItemIndices
        }
    }
}