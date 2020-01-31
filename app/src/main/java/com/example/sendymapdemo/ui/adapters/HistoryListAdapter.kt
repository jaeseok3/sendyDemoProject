package com.example.sendymapdemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.HistoryData
import kotlinx.android.synthetic.main.history_item.view.*

class HistoryListAdapter(private val items: List<HistoryData>) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("on bind view holder")
        val listener = View.OnClickListener {it ->
            Toast.makeText(it.context, "${items[position].Src}입니다!", Toast.LENGTH_SHORT).show()
        }
        holder.apply {
            bind(listener, items)
            itemView.tag = items
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, items: List<HistoryData>) {
            view.historyDate.text = items[position].HistoryDate
            view.historyTime.text = items[position].HistoryTime
            view.historySrc.text = items[position].Src
            view.historyDst.text = items[position].Dest
//            view.historyDuration.text = items[position].Distance
//            view.historyTime_amount.text = items[position].Time
            view.historyReward.text = items[position].Reward
            view.setOnClickListener(listener)
        }
    }
}