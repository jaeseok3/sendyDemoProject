package com.example.sendymapdemo.ui.adapters

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.RequestListData
import com.example.sendymapdemo.ui.activities.MapsActivity
import kotlinx.android.synthetic.main.request_listview_item.view.*

class RequestRecyclerAdapter (private val dataSource: ArrayList<RequestListData>): RecyclerView.Adapter<RequestRecyclerAdapter.ViewHolder>() {
    interface OnItemClickListener {
        fun onItemClickListener(view: View, position: Int)
    }
    var itemClick: OnItemClickListener ?= null

    inner class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        private val view: View = v
        fun bind(items: ArrayList<RequestListData>) {
            view.requestFeeling.setImageResource(items[position].image)
            view.requestSrc.text = items[position].source
            view.requestDst.text = items[position].destination
            view.requestTime.text = items[position].time
            view.requestDuration.text = items[position].distance
            view.requestReward.text = items[position].reward.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.request_listview_item, parent, false)
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