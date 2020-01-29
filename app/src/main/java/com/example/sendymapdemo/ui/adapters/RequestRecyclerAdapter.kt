package com.example.sendymapdemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.RequestListData
import kotlinx.android.synthetic.main.new_request_item.view.*

class RequestRecyclerAdapter (private val dataSource: ArrayList<RequestListData>): RecyclerView.Adapter<RequestRecyclerAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int)
    }
    var itemClick: OnItemClickListener ?= null

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val view: View = v
        fun bind(items: ArrayList<RequestListData>) {
            view.clockImage.setImageResource(items[position].image)
            view.srcText.text = items[position].source
            view.dstText.text = items[position].destination
            view.time.text = items[position].time
            view.distance.text = items[position].distance
            view.creditText.text = items[position].reward.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.new_request_item, parent, false)
        return ViewHolder(inflatedView)
    }

    override fun getItemCount(): Int = dataSource.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSource)
        holder.itemView.setOnClickListener {
            itemClick?.onItemClickListener(it, position)
        }
    }
}