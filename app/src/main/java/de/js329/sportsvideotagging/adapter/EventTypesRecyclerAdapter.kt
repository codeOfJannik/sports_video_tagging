package de.js329.sportsvideotagging.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.datamodels.EventType
import de.js329.sportsvideotagging.inflate

class EventTypesRecyclerAdapter(val mClickListener: ItemClickListener, private val eventTypes : List<EventType>): RecyclerView.Adapter<EventTypesRecyclerAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val eventTitleTextView: TextView = itemView.findViewById(R.id.eventTypeTitleTextView)

        override fun onClick(v: View?) {
            mClickListener.onItemClick(v, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.event_type_recycler_item, false)
        val holder = ViewHolder(view)
        view.setOnClickListener { holder.onClick(view) }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.eventTitleTextView.text = eventTypes[position].eventTitle
    }

    override fun getItemCount(): Int {
        return eventTypes.size
    }

    fun getItem(position: Int): EventType {
        return eventTypes[position]
    }

    interface ItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }
}