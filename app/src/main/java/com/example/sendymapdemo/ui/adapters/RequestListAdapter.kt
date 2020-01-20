package com.example.sendymapdemo.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataClass.RequestListData

class RequestListAdapter(context: Context, private val dataSource: ArrayList<RequestListData>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    //1
    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): RequestListData {
        return dataSource[position]
    }

    //3
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //4
    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Get view for row item
        val rowView = inflater.inflate(R.layout.request_listview_item, parent, false)

        val requestImg: ImageView = rowView.findViewById(R.id.requestFeeling)
        val requestSrc: TextView = rowView.findViewById(R.id.requestSrc)
        val requestDst: TextView = rowView.findViewById(R.id.requestDst)
        val requestTime: TextView = rowView.findViewById(R.id.requestTime)
        val requestDuration: TextView = rowView.findViewById(R.id.requestDuration)
        val requestReward: TextView = rowView.findViewById(R.id.requestReward)

        val item = getItem(position)
        requestImg.setImageResource(item.image)
        requestSrc.setText(item.source)
        requestDst.setText(item.destination)
        requestTime.setText(item.time)
        requestDuration.setText(item.distance)
        requestReward.setText(item.reward.toString())

        return rowView
    }
}