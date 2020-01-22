package com.example.sendymapdemo.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.model.repository.HistoryRepository
import kotlinx.android.synthetic.main.history_activiry.*
import org.koin.android.ext.android.inject

////히스토리 리스트
//var historyList = ArrayList<HistoryData>()

class HistoryActivity : AppCompatActivity(){

    private lateinit var historylayoutManager: LinearLayoutManager

    private val historyRepository: HistoryRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activiry)
    }

    override fun onResume() {
        super.onResume()
        //레이아웃 매니저
        historylayoutManager = LinearLayoutManager(this)

        historyRecyclerList.adapter = historyRepository.historyAdapter
        historyRecyclerList.layoutManager = historylayoutManager
        historyRecyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
}

}