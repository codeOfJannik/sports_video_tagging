package de.js329.sportsvideotagging.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.activities.EventTypeChangedListener
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.datamodels.LongTimedEventType
import de.js329.sportsvideotagging.toTimeOffsetString

class EventTypesAdapter(private val context: Context, var allEventTypes: List<EventType>, var allLongTimedEventTypes: List<LongTimedEventType>, private val eventTypeChangedListener: EventTypeChangedListener): BaseAdapter() {
    override fun getCount(): Int {
        return allEventTypes.size + allLongTimedEventTypes.size + 2
    }

    override fun getItem(position: Int): Any {
        if (position == 0) { return "Event Types" }
        if (position - 1 < allEventTypes.size) { return allEventTypes[position - 1] }
        if (position - 1 == allEventTypes.size) { return "Long Timed Event Types" }
        if (position - 1 > allEventTypes.size) { return allLongTimedEventTypes[position - allEventTypes.size - 2] }
        return "null"
    }

    override fun getItemId(position: Int): Long {
        when (val item = getItem(position)) {
            is EventType -> return item.uid ?: -1
            is LongTimedEventType -> return item.uid ?: -1
            is String -> return -1
        }
        return -1
    }

    override fun getItemViewType(position: Int): Int {
        when (getItem(position)) {
            is EventType -> return 0
            is LongTimedEventType -> return 1
            is String -> return 2
        }
        return  -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view: View = View(context)
        when (val item = getItem(position)) {
            is String -> {
                if (convertView == null || convertView.tag != 2) {
                    view = LayoutInflater.from(context).inflate(R.layout.list_header, parent, false)
                    view.tag = 2
                } else {
                    view = convertView
                }
                view.findViewById<TextView>(R.id.separator).text = item
            }
            is EventType -> {
                if (convertView == null || convertView.tag != 0) {
                    view = LayoutInflater.from(context).inflate(R.layout.layout_list_view_event_type_row, parent, false)
                    view.tag = 0
                } else {
                    view = convertView
                }
                val timeOffset = item.timeOffset
                view.findViewById<TextView>(R.id.eventTitleTextView).text = item.eventTitle
                view.findViewById<TextView>(R.id.timeOffsetTextView).text = context.getString(R.string.timeOffsetWithVars, timeOffset.toTimeOffsetString())
                view.findViewById<ImageView>(R.id.playerSelectionCheckImageView).setImageResource(
                        if (item.playerSelection) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_clear_24
                )
                view.findViewById<ImageView>(R.id.attributesAllowedCheckImageView).setImageResource(
                        if (item.attributesAllowed) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_clear_24
                )

                // val activeCheckBox = view.findViewById<CheckBox>(R.id.activeEventTypeCheckBox)
                // activeCheckBox.isChecked = item.activeEventType
                // activeCheckBox.setOnCheckedChangeListener { _, isChecked ->
                //     eventTypeChangedListener.onEventTypeActiveChanged(item, isChecked)
                // }
            }
            is LongTimedEventType -> {
                if (convertView == null || convertView.tag != 1) {
                    view = LayoutInflater.from(context).inflate(R.layout.layout_list_view_long_timed_event_type, parent, false)
                    view.tag = 1
                } else {
                    view = convertView
                }
                view.findViewById<TextView>(R.id.eventATitleTextView).text = item.eventATitle
                view.findViewById<ImageView>(R.id.swapImageView).isVisible = item.switchable
                view.findViewById<TextView>(R.id.eventBTitleTextView).apply {
                    isVisible = item.switchable
                    if (isVisible) {
                        text = item.eventBTitle
                    }
                }
                // val activeCheckBox = view.findViewById<CheckBox>(R.id.activeEventTypeCheckBox)
                // activeCheckBox.isChecked = item.activeEventType
                // activeCheckBox.setOnCheckedChangeListener { _, isChecked ->
                //     eventTypeChangedListener.onEventTypeActiveChanged(item, isChecked)
                // }
            }
        }
        return view
    }
}