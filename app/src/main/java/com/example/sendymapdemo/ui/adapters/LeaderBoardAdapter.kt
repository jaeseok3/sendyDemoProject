package com.example.sendymapdemo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.UserData
import kotlinx.android.synthetic.main.navigation_recyclerview_item.view.*

class LeaderBoardAdapter(private val items: List<UserData>) : RecyclerView.Adapter<LeaderBoardAdapter.ViewHolder>() {
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("on bind view holder")
        val item = items[position]
        val listener = View.OnClickListener {
            Toast.makeText(it.context, "${item.id}입니다!", Toast.LENGTH_SHORT).show()
        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
            ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context)
            .inflate(R.layout.navigation_recyclerview_item, parent, false)
        return ViewHolder(inflatedView)
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v
        fun bind(listener: View.OnClickListener, item: UserData) {
            view.boardName.text = item.id
            view.boardRank.text = item.rank
            view.boardAccum.text = item.credit
            view.setOnClickListener(listener)
        }
    }
}