package de.js329.sportsvideotagging.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import de.js329.sportsvideotagging.R
import de.js329.sportsvideotagging.datamodels.Player

class InnerRecyclerViewAdapter(
    private val context: Context,
    private val players: List<Player>,
    private val mPlayerSelectionListener: PlayerSelectionListener
): RecyclerView.Adapter<InnerRecyclerViewAdapter.ChildViewHolder>() {

    private val playerSelected = BooleanArray(players.size) { return@BooleanArray false }

    inner class ChildViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val rootView: View = itemView.findViewById(R.id.expandableChildRootView)
        val checkmark: ImageView = itemView.findViewById(R.id.checkmarkImage)
        val textView1: TextView = itemView.findViewById(R.id.text1)
        val textView2: TextView = itemView.findViewById(R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InnerRecyclerViewAdapter.ChildViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_expandable_recyclerview_child, parent, false)
        return ChildViewHolder((view))
    }

    override fun onBindViewHolder(holder: InnerRecyclerViewAdapter.ChildViewHolder, position: Int) {
        val player = players[position]
        holder.textView1.text = player.number.toString()
        holder.textView2.text = player.name
        holder.rootView.setOnClickListener {
            if (!playerSelected[position]) {
                playerSelected[position] = true
                mPlayerSelectionListener.onPlayerSelected(player)
                holder.rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.light_grey))
                holder.checkmark.visibility = View.VISIBLE
            } else {
                playerSelected[position] = false
                mPlayerSelectionListener.onPlayerUnselected(player)
                holder.rootView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
                holder.checkmark.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return players.size
    }

    interface PlayerSelectionListener {
        fun onPlayerSelected(player: Player)
        fun onPlayerUnselected(player: Player)
    }
}