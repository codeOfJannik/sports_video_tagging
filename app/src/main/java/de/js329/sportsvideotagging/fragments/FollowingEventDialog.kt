package de.js329.sportsvideotagging.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.adapter.EventTypesRecyclerAdapter
import de.js329.sportsvideotagging.adapter.ExpandableRecyclerViewAdapter
import de.js329.sportsvideotagging.adapter.InnerRecyclerViewAdapter
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.datamodels.EventAttribute
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.datamodels.Player
import kotlinx.coroutines.launch

class FollowingEventDialog: DialogFragment(), EventTypesRecyclerAdapter.ItemClickListener, InnerRecyclerViewAdapter.PlayerSelectionListener {

    private lateinit var toolbar: Toolbar
    private lateinit var frameLayout: FrameLayout
    private lateinit var containerHeaderTextView: TextView
    private lateinit var addAttributesBtn: Button
    private lateinit var onFollowUpSaved: (EventType) -> Unit

    private lateinit var eventTypesAdapter: EventTypesRecyclerAdapter
    private lateinit var matchTaggingController: MatchTaggingController
    private var followUpEvent: EventType? = null
    private val selectedPlayers: MutableSet<Player> = HashSet()
    private val checkedAttributes: MutableMap<EventAttribute, Boolean> = HashMap()

    companion object {
        private const val TAG = "following_event_dialog"

        fun display(fragmentManager: FragmentManager, mtc: MatchTaggingController, onSaveCallback: (eventType: EventType) -> Unit): FollowingEventDialog {
            val dialog = FollowingEventDialog()
            dialog.matchTaggingController = mtc
            dialog.onFollowUpSaved = onSaveCallback
            dialog.show(fragmentManager, TAG)
            return dialog
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Make dialog fullscreen here
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.layout_following_event_recyclerview_dialog, container, false)
        toolbar = view.findViewById(R.id.toolbar)
        frameLayout = view.findViewById(R.id.containerView)
        containerHeaderTextView = view.findViewById(R.id.containerHeaderTextView)
        addAttributesBtn = view.findViewById(R.id.addAttributesBtn)
        setupEventSelectionRecyclerView()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            setLayout(width, height)
        }
    }

    private fun setupEventSelectionRecyclerView() {
        containerHeaderTextView.text = requireContext().getString(R.string.headerFollowUpEventSelection)
        val recyclerView = RecyclerView(requireContext())
        lifecycleScope.launch {
            val eventTypes = matchTaggingController.getEventTypes()
                    .toMutableList().apply {
                        removeIf { it.eventTitle == "Match Start" }
                    }
            eventTypesAdapter = EventTypesRecyclerAdapter(this@FollowingEventDialog, eventTypes)
            recyclerView.adapter = eventTypesAdapter
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.HORIZONTAL))
            recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL))
            frameLayout.addView(recyclerView)
        }
    }

    private fun setupPlayerSelection() {
        containerHeaderTextView.text = requireContext().getString(R.string.headerFollowUpPlayerSelection)
        frameLayout.removeAllViews()
        toolbar.inflateMenu(R.menu.followup_event_dialog)
        toolbar.setOnMenuItemClickListener { onSaveFollowUpClicked() }
        addAttributesBtn.visibility = View.VISIBLE
        val recyclerView = RecyclerView(requireContext())

        val homeTeam = matchTaggingController.homeTeam
        val awayTeam = matchTaggingController.awayTeam
        val teams = listOf(homeTeam, awayTeam)
        val players: MutableList<List<Player>> = ArrayList()

        addAttributesBtn.setOnClickListener { onAddAttributesClicked() }

        lifecycleScope.launch {
            val homeTeamPlayers = matchTaggingController.getPlayersForTeam(homeTeam).sortedBy { it.number }
            players.add(homeTeamPlayers)
            val awayTeamPlayers = matchTaggingController.getPlayersForTeam(awayTeam).sortedBy { it.number }
            players.add(awayTeamPlayers)
            recyclerView.adapter = ExpandableRecyclerViewAdapter(requireContext(), teams, players, this@FollowingEventDialog)
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            frameLayout.addView(recyclerView)
        }
    }

    private fun setupAttributeSelection() {
        containerHeaderTextView.text = requireContext().getString(R.string.selectAttributesForFollowUp)
        frameLayout.removeAllViews()
        addAttributesBtn.visibility = View.GONE

        val linearLayout = LinearLayout(requireContext())
        linearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout.orientation = LinearLayout.VERTICAL

        val scrollView = ScrollView(requireContext())
        scrollView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
        scrollView.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        lifecycleScope.launch {
            val attributes = matchTaggingController.getEventAttributes()
            attributes.forEach {
                val checkBox = CheckBox(requireContext())
                checkBox.text = it.attribute_name
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    checkedAttributes[it] = isChecked
                }
                linearLayout.addView(checkBox)
                checkedAttributes[it] = false
            }
            scrollView.addView(linearLayout)
            frameLayout.addView(scrollView)
        }

    }

    private fun onSaveFollowUpClicked(): Boolean {
        val attributes = checkedAttributes.filter { it.value }.keys.toList()
        followUpEvent?.let {
            lifecycleScope.launch {
                val success = matchTaggingController.addFollowUpEvent(it, selectedPlayers.toList(), attributes)
                onFollowUpSaved(it)
                this@FollowingEventDialog.dismiss()
            }
        }
        return true
    }

    private fun onAddAttributesClicked() {
        setupAttributeSelection()
    }


    override fun onItemClick(view: View?, position: Int) {
        followUpEvent = eventTypesAdapter.getItem(position)
        setupPlayerSelection()
    }

    override fun onPlayerSelected(player: Player) {
        selectedPlayers.add(player)
    }

    override fun onPlayerUnselected(player: Player) {
        selectedPlayers.remove(player)
    }
}