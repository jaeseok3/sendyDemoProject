package com.example.sendymapdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.navigation_recyclerview_item.view.*
var ia:Int = 0

class leaderBoardAdapter(private val items: ArrayList<userInfo>) : RecyclerView.Adapter<leaderBoardAdapter.ViewHolder>() {
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: leaderBoardAdapter.ViewHolder, position: Int) {
        println("on bind view holder")
        val item = items[position]
        val listener = View.OnClickListener {it ->
            Toast.makeText(it.context, "${item.ID}입니다!", Toast.LENGTH_SHORT).show()
            drawerLayout.closeDrawers()
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            leaderBoardAdapter.ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.navigation_recyclerview_item, parent, false)
        return leaderBoardAdapter.ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private  var ii=1
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: userInfo) {
            view.boardName.text = item.ID
            view.boardRank.text = item.rank.toString()
            view.boardAccum.text = item.accumulatedCredit.toString()
            view.setOnClickListener(listener)
        }
    }
}