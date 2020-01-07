package com.example.sendymapdemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class requestAdapter(private val context: Context,
                     private val dataSource: ArrayList<request>) : BaseAdapter(){
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    //1
    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.request_listview_item, parent, false)
        val srcView = rowView.findViewById<TextView>(R.id.requestSrc)
        val dstView = rowView.findViewById<TextView>(R.id.requestDst)
        val durationView = rowView.findViewById<TextView>(R.id.requestDuration)
        val rewardView = rowView.findViewById<TextView>(R.id.requestDuration)
        val feelingView = rowView.findViewById<ImageView>(R.id.requestFeeling)
        return rowView
    }
}