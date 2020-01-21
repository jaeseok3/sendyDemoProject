package com.example.sendymapdemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.HistoryData
import kotlinx.android.synthetic.main.history_item.view.*

class HistoryListAdapter(private val items: HistoryData) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {
    override fun getItemCount() = items.result!!.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("on bind view holder")
        val listener = View.OnClickListener {it ->
            Toast.makeText(it.context, "${items.result!![position].Src}입니다!", Toast.LENGTH_SHORT).show()
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
        fun bind(listener: View.OnClickListener, items: HistoryData) {
            view.historyDate.text = items.result!![position].HistoryDate
            view.historyTime.text = items.result[position].HistoryTime
            view.historySrc.text = items.result[position].Src
            view.historyDst.text = items.result[position].Dest
            view.historyDuration.text = items.result[position].Distance
            view.historyTime_amount.text = items.result[position].Time
            view.historyReward.text = items.result[position].Reward
            view.setOnClickListener(listener)
        }
    }
}