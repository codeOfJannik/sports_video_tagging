package de.js329.sportsvideotagging.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.datamodels.LongTimedEventType
import de.js329.sportsvideotagging.inflate

class LongTimedEventTypesAdapter(val mClickListener: ItemClickListener, private val longTimedEventTypes: List<LongTimedEventType>, val context: Context): RecyclerView.Adapter<LongTimedEventTypesAdapter.ViewHolder>() {

    private val itemList = createLongTimedItemList()
    val selectedItemIndeces = ArrayList<Int>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val eventTitleTextView: TextView = itemView.findViewById(R.id.eventTypeTitleTextView)
        val rootView = itemView.findViewById<ConstraintLayout>(R.id.rootView)

        override fun onClick(v: View?) {
            if (isSwitchableItem(adapterPosition)) mClickListener.onSwitchableItemClicked(v,  adapterPosition) else mClickListener.onToggleableItemClicked(v, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = parent.inflate(R.layout.event_type_recycler_item, false)
        val holder = ViewHolder(view)
        view.setOnClickListener { holder.onClick(view) }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.eventTitleTextView.text = getStringItem(position)
        if (selectedItemIndeces.contains(position)) {
            holder.rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
        } else {
            holder.rootView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        }
    }

    override fun getItemCount(): Int {
        return itemList.map { it.second }.sum()
    }

    fun getStringItem(position: Int): String {
        var positionIndex = position
        for (item in itemList) {
            if (positionIndex > 1 || (positionIndex == 1 && item.second == 1)) {
                positionIndex -= item.second
                continue
            }
            if (positionIndex == 1 && item.second == 2) {
                return item.first.eventBTitle ?: "Event B"
            }
            return item.first.eventATitle
        }
        return "null"
    }

    fun getItem(position: Int): Pair<LongTimedEventType, Int>? {
        var positionIndex = position
        for (item in itemList) {
            if (positionIndex > 1 || (positionIndex == 1 && item.second == 1)) {
                positionIndex -= item.second
                continue
            }
            return item
        }
        return null
    }

    private fun createLongTimedItemList(): List<Pair<LongTimedEventType, Int>> {
        val itemList: MutableList<Pair<LongTimedEventType, Int>> = ArrayList()
        longTimedEventTypes.forEach {
            itemList.add(Pair(it, if (it.switchable) 2 else 1))
        }
        return itemList
    }

    fun isSwitchableItem(position: Int): Boolean {
        getItem(position)?.let { return it.first.switchable }
        return false
    }

    interface ItemClickListener {
        fun onSwitchableItemClicked(view: View?, position: Int)
        fun onToggleableItemClicked(view: View?, position: Int)
    }
}