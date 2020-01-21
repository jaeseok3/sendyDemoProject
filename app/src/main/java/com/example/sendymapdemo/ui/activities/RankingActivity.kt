package com.example.sendymapdemo.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataClass.AllUserData
import com.example.sendymapdemo.dataClass.UserData
import com.example.sendymapdemo.model.repository.UserRepository
import com.example.sendymapdemo.ui.adapters.LeaderBoardAdapter
import kotlinx.android.synthetic.main.ranking_activity.*
import org.koin.android.ext.android.inject

//리더보드 어댑터
lateinit var boardAdapter: LeaderBoardAdapter

class rankingActivity : AppCompatActivity(){
    private val userRepository: UserRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_activity)

        userRepository.getData(userRepository.userID)
        //리더보드 어댑터 초기화
        boardAdapter = LeaderBoardAdapter(userRepository.getAllUsers())
        //리더보드 레이아웃 매니저
        boardAdapter.notifyDataSetChanged()
        val layoutManager = LinearLayoutManager(this)

        recyclerList.adapter = boardAdapter
        recyclerList.layoutManager = layoutManager
        recyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}