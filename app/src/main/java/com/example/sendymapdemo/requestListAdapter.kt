package com.example.sendymapdemo

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class requestListAdapter(context: Context,
                         private val dataSource: ArrayList<requestInfo>) : BaseAdapter() {

    private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    //1
    override fun getCount(): Int {
        return dataSource.size
    }

    //2
    override fun getItem(position: Int): requestInfo {
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

        var requestImg: ImageView = rowView.findViewById(R.id.requestFeeling)
        var requestSrc: TextView = rowView.findViewById(R.id.requestSrc)
        var requestDst: TextView = rowView.findViewById(R.id.requestDst)
        var requestTime: TextView = rowView.findViewById(R.id.requestTime)
        var requestDuration: TextView = rowView.findViewById(R.id.requestDuration)
        var requestReward: TextView = rowView.findViewById(R.id.requestReward)

        var item = getItem(position)
        requestImg.setImageResource(item.image)
        requestSrc.setText(item.source)
        requestDst.setText(item.destination)
        requestTime.setText(item.time)
        requestDuration.setText(item.distance)
        requestReward.setText(item.reward.toString())


        return rowView
    }
}