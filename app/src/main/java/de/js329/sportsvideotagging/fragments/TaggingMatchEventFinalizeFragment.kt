package de.js329.sportsvideotagging.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.activities.TaggingFragmentManager
import de.js329.sportsvideotagging.controller.MatchTaggingController
import de.js329.sportsvideotagging.datamodels.EventAttribute
import de.js329.sportsvideotagging.datamodels.EventType
import kotlinx.coroutines.launch

class TaggingMatchEventFinalizeFragment: Fragment() {

    private lateinit var matchTaggingController: MatchTaggingController
    private lateinit var taggingFragmentManager: TaggingFragmentManager

    private lateinit var attributesTextView: TextView
    private lateinit var followingEventTextView: TextView
    private lateinit var addFollowUpEventBtn: Button

    private lateinit var attributes: List<EventAttribute>
    private lateinit var selectedAttributes: BooleanArray
    private var attributeSelectionEnabled = true

    companion object {
        fun newInstance(matchTaggingController: MatchTaggingController, taggingFragmentManager: TaggingFragmentManager) = TaggingMatchEventFinalizeFragment().apply {
            this.matchTaggingController = matchTaggingController
            this.taggingFragmentManager = taggingFragmentManager
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_match_event_finalize, container, false)
        setupAttributesDialogButton(view)

        followingEventTextView = view.findViewById(R.id.followingEventTextView)
        followingEventTextView.text = requireContext().getString(R.string.noFollowingEvent)

        view.findViewById<Button>(R.id.saveMatchEvent_Btn).setOnClickListener { finishEventCreation() }
        addFollowUpEventBtn = view.findViewById(R.id.addFollowingEventBtn)
        addFollowUpEventBtn.setOnClickListener { onAddFollowingEventClicked() }
        return view
    }

    private fun setupAttributesDialogButton(root: View) {
        val attributesButton = root.findViewById<Button>(R.id.addAttributesBtn)
        val eventType = matchTaggingController.getEventType()
        eventType?.let {
            if (it.attributesAllowed) {
                lifecycleScope.launch {
                    attributes = matchTaggingController.getEventAttributes()
                    attributesTextView = root.findViewById(R.id.attributeListTxtView)
                    selectedAttributes = BooleanArray(attributes.size) { return@BooleanArray false }
                    attributesButton.setOnClickListener { onAddAttributesClicked() }
                }
                return
            }
        }
        attributeSelectionEnabled = false
        attributesButton.visibility = View.GONE
    }

    private fun onAddFollowingEventClicked() {
        FollowingEventDialog.display(childFragmentManager, matchTaggingController, ::onFollowUpAdded)
    }

    private fun onFollowUpAdded(eventType: EventType) {
        followingEventTextView.text = requireContext().getString(R.string.followUpEventAdded, eventType.eventTitle)
        addFollowUpEventBtn.isEnabled = false
    }

    private fun finishEventCreation() {
        if (attributeSelectionEnabled) {
            lifecycleScope.launch {
                matchTaggingController.addEventAttributes(getSelectedAttributes())
            }
        }
        matchTaggingController.finishMatchEventCreation()
        taggingFragmentManager.switchToBaseFragment()
    }

    private fun onAddAttributesClicked() {
        lifecycleScope.launch {
            val attributeNames = attributes.map { it.attribute_name }
            val alertBuilder = AlertDialog.Builder(requireContext())
            alertBuilder
                    .setTitle(R.string.addAttributesToEventDialogTitle)
                    .setMultiChoiceItems(attributeNames.toTypedArray(), selectedAttributes) { _, which, isChecked ->
                        selectedAttributes[which] = isChecked
                    }
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        var attributesString = ""
                        if (selectedAttributes.contains(true)) {
                            attributesString = "Selected Attributes:"
                            attributes.mapIndexed { index, eventAttribute ->
                                if (selectedAttributes[index]) {
                                    attributesString += " " + eventAttribute.attribute_name + ","
                                }
                            }
                        }
                        attributesTextView.text = attributesString.trim(',')
                    }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .show()
        }
    }

    private fun getSelectedAttributes(): List<EventAttribute> {
        return attributes.filterIndexed { index, _ ->
            selectedAttributes[index]
        }
    }
}