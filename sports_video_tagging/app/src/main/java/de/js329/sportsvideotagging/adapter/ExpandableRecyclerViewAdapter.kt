package de.js329.sportsvideotagging.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.datamodels.Player
import de.js329.sportsvideotagging.datamodels.Team

class ExpandableRecyclerViewAdapter(
        private val context: Context,
        private val teams: List<Team>,
        private val players: List<List<Player>>,
        private val mPlayerSelectionListener: InnerRecyclerViewAdapter.PlayerSelectionListener
): RecyclerView.Adapter<ExpandableRecyclerViewAdapter.ParentViewHolder>() {

    class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.parentNameItemTextView)
        val expandIcon: ImageView = itemView.findViewById(R.id.expandIconImageView)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.eventTypesRecyclerView)
        val cardView: CardView = itemView.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_expandable_recyclerview_parent, parent, false)
        return ParentViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        holder.textView.text = teams[position].teamName
        val childAdapter = InnerRecyclerViewAdapter(context, players[position], mPlayerSelectionListener)
        holder.recyclerView.layoutManager = GridLayoutManager(context, 2)
        holder.cardView.setOnClickListener {
            if (holder.recyclerView.isVisible) {
                holder.recyclerView.visibility = View.GONE
                holder.expandIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
            } else {
                holder.recyclerView.visibility = View.VISIBLE
                holder.expandIcon.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
            }
        }
        holder.recyclerView.adapter = childAdapter
    }

    override fun getItemCount(): Int {
        return teams.size
    }
}