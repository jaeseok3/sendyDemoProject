package com.example.sendymapdemo.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.ui.adapters.LeaderBoardAdapter
import com.example.sendymapdemo.viewmodel.RankingViewModel
import kotlinx.android.synthetic.main.ranking_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RankingActivity : AppCompatActivity(){
    private val rankingViewModel by viewModel<RankingViewModel>()

    private lateinit var leaderBoardLayoutManager: LinearLayoutManager
    private lateinit var adapter: LeaderBoardAdapter
    private var newUserDataList: List<UserData> ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_activity)

        rankingViewModel.getAllUserData()
        subscribe()
    }

    private fun subscribe(){
        val userListObserver = Observer<List<UserData>> {
            newUserDataList = it
            recyclerViewSetup()
        }
        rankingViewModel.userList?.observe(this, userListObserver)
    }

    private fun recyclerViewSetup() {
        adapter = LeaderBoardAdapter(newUserDataList!!)
        leaderBoardLayoutManager = LinearLayoutManager(this)

        leaderBoard_recyclerList.adapter = adapter
        leaderBoard_recyclerList.layoutManager = leaderBoardLayoutManager
        adapter.notifyDataSetChanged()
    }
}