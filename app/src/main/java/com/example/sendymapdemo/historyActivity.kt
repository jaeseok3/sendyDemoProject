package com.example.sendymapdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.dataClass.HistoryData
import kotlinx.android.synthetic.main.history_activiry.*

//히스토리 리스트
var historyList = ArrayList<HistoryData>()

class historyActivity : AppCompatActivity(){
    private lateinit var historyAdapter:historyListAdapter
    private lateinit var historylayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activiry)

        //어댑터 초기화
        historyList.clear()
        GetHistory(userIdentity)
        historyAdapter = historyListAdapter(historyList)
        historyAdapter.notifyDataSetChanged()
        //레이아웃 매니저
        historylayoutManager = LinearLayoutManager(this)

        historyRecyclerList.adapter = historyAdapter
        historyRecyclerList.layoutManager = historylayoutManager
        historyRecyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
//        historyAdapter= historyListAdapter(historyList)

    }

}