package com.example.sendymapdemo.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.ui.adapters.HistoryListAdapter
import com.example.sendymapdemo.viewmodel.HistoryViewModel
import kotlinx.android.synthetic.main.history_activiry.*
import kotlinx.android.synthetic.main.ranking_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryActivity : AppCompatActivity(){
    private val historyViewModel by viewModel<HistoryViewModel>()

    private lateinit var historylayoutManager: LinearLayoutManager
    private lateinit var adapter: HistoryListAdapter
    private var newHistoryList : List<HistoryData> ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_activiry)

        historyViewModel.getHistory()
        subscribe()

        back_button_history.setOnClickListener {
            super.onBackPressed()
        }
    }

    private fun subscribe(){
        val historyListObserver = Observer<List<HistoryData>> {
            newHistoryList = it
            recyclerViewSetup()
        }
        historyViewModel.historyList?.observe(this, historyListObserver)
    }

    private fun recyclerViewSetup() {
        adapter = HistoryListAdapter(newHistoryList!!)
        historylayoutManager = LinearLayoutManager(this)

        historyRecyclerList.adapter = adapter
        historyRecyclerList.layoutManager = historylayoutManager
    }
}