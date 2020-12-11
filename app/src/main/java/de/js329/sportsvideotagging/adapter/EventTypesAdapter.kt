package de.js329.sportsvideotagging.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.activities.EventTypeChangedListener
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.toTimeOffsetString

class EventTypesAdapter(private val context: Context, var allEventTypes: List<EventType>, private val eventTypeChangedListener: EventTypeChangedListener): BaseAdapter() {
    override fun getCount(): Int {
        return allEventTypes.size
    }

    override fun getItem(position: Int): EventType {
        return allEventTypes[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).uid ?: -1
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val item = getItem(position)
        val timeOffset = item.timeOffset

        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.layout_list_view_event_type_row, parent, false)
        view.findViewById<TextView>(R.id.eventTitleTextView).text = item.eventTitle
        view.findViewById<TextView>(R.id.timeOffsetTextView).text = context.getString(R.string.timeOffsetWithVars, timeOffset.toTimeOffsetString())
        view.findViewById<ImageView>(R.id.longTimedEventCheckImageView).setImageResource(
                if (item.longTimedEvent) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_clear_24
        )
        view.findViewById<ImageView>(R.id.playerSelectionCheckImageView).setImageResource(
                if (item.playerSelection) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_clear_24
        )
        view.findViewById<ImageView>(R.id.attributesAllowedCheckImageView).setImageResource(
                if (item.attributesAllowed) R.drawable.ic_baseline_check_24 else R.drawable.ic_baseline_clear_24
        )

        val activeCheckBox = view.findViewById<CheckBox>(R.id.activeEventTypeCheckBox)
        activeCheckBox.isChecked = item.activeEventType
        activeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            eventTypeChangedListener.onEventTypeActiveChanged(item, isChecked)
        }
        return view
    }
}