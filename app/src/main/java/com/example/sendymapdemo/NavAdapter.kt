package com.example.sendymapdemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.navigation_recyclerview_item.view.*

class NavAdapter(private val items: ArrayList<markerData>) : RecyclerView.Adapter<NavAdapter.ViewHolder>() {

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: NavAdapter.ViewHolder, position: Int) {
        val item = items[position]
        val listener = View.OnClickListener {it ->
            Toast.makeText(it.context, "${item.nameBy}입니다!", Toast.LENGTH_SHORT).show()
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            NavAdapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.navigation_recyclerview_item, parent, false)
        return NavAdapter.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: markerData) {
            view.item_name.text = item.nameBy
            view.lat.text = item.lati.toString()
            view.lng.text = item.longi.toString()
            view.setOnClickListener(listener)
        }
    }
}