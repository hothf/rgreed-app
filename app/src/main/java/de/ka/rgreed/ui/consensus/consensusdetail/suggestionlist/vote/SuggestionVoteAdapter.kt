package de.ka.rgreed.ui.consensus.consensusdetail.suggestionlist.vote

import android.app.Application
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.ka.rgreed.R
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject

/**
 * Creates a new suggestion vote adapter. The range of valid values is [0..10].
 */
class SuggestionVoteAdapter(val itemClickListener: ((Int) -> Unit)? = null) :
    RecyclerView.Adapter<SuggestionVoteAdapter.VoteViewHolder>(), KoinComponent {

    val app: Application by inject()

    private val dataSet: List<Pair<Int, String>> = (0..10).map {
        when (it) {
            0 -> Pair(it, app.getString(R.string.suggestions_vote_noobjections))
            5 -> Pair(it, app.getString(R.string.suggestions_vote_ok))
            10 -> Pair(it, app.getString(R.string.suggestions_vote_objections))
            else -> Pair(it, "")
        }
    }

    /**
     * Retrieves the data at the given index.
     */
    fun getDataAt(position: Int): Pair<Int, String>? {
        return if (position < 0 || position > dataSet.size - 1) {
            null
        } else {
            dataSet[position]
        }
    }

    /**
     * Retrieves the position of the item resembling the value given. If the value given is null, returns 0.
     */
    fun getPositionForValue(value: Float?): Int? {
        if (value == null) {
            return 0
        }
        val item = dataSet.find { it.first == value.toInt() }

        if (item != null) {
            return dataSet.indexOf(item)
        }
        return 0
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoteViewHolder {
        return VoteViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_vote_pick, parent, false))
    }

    override fun onBindViewHolder(holder: VoteViewHolder, position: Int) {

        val data = getDataAt(holder.adapterPosition)

        holder.itemView.findViewById<TextView>(R.id.pickText).text = data?.first.toString()
        holder.itemView.findViewById<TextView>(R.id.pickInfo).text = data?.second

        holder.itemView.setOnClickListener {
            itemClickListener?.invoke(holder.adapterPosition)
        }

        if (data?.second.isNullOrEmpty()) {
            holder.itemView.findViewById<TextView>(R.id.pickInfo).visibility = View.GONE
        } else {
            holder.itemView.findViewById<TextView>(R.id.pickInfo).visibility = View.VISIBLE
        }
    }

    class VoteViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}

