package com.example.sendymapdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.history_item.view.*
import kotlinx.android.synthetic.main.navigation_recyclerview_item.view.*

class historyListAdapter(private val items: ArrayList<historyInfo>) : RecyclerView.Adapter<historyListAdapter.ViewHolder>() {
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: historyListAdapter.ViewHolder, position: Int) {
        println("on bind view holder")
        val item = items[position]
        val listener = View.OnClickListener {it ->
            Toast.makeText(it.context, "${item.source}입니다!", Toast.LENGTH_SHORT).show()

        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_item, parent, false)
        return historyListAdapter.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: historyInfo) {
            view.historyDate.text = item.historyDate
            view.historyTime.text = item.historyTime
            view.historySrc.text = item.source
            view.historyDst.text = item.destination
            view.historyDuration.text = item.distance
            view.historyTime_amount.text = item.time
            view.historyReward.text = item.reward.toString()
            view.setOnClickListener(listener)
        }
    }
}