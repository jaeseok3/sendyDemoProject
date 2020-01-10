package com.example.sendymapdemo

import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.ranking_activity.*

//리더보드 어댑터
lateinit var boardAdapter:leaderBoardAdapter

//유저들의 정보를 담은 리스트
var userList = ArrayList<userInfo>()

class rankingActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ranking_activity)
        login(userIdentity)
        //리더보드 어댑터 초기화
        boardAdapter = leaderBoardAdapter(userList)
        //리더보드 레이아웃 매니저
        layoutManager = LinearLayoutManager(this)

        recyclerList.adapter = boardAdapter
        recyclerList.layoutManager = layoutManager
        recyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }
}