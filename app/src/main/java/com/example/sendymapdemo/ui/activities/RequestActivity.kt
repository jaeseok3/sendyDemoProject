package com.example.sendymapdemo.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.Toast.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.sendymapdemo.R
import com.example.sendymapdemo.SetPathUI
import com.example.sendymapdemo.dataclass.*
import com.example.sendymapdemo.model.repository.*
import com.example.sendymapdemo.ui.adapters.RequestListAdapter
import com.example.sendymapdemo.ui.adapters.RequestRecyclerAdapter
import com.naver.maps.geometry.LatLng
import org.koin.android.ext.android.inject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RequestActivity : AppCompatActivity() {
    private lateinit var requestListView: RecyclerView
    private lateinit var adapter: RequestRecyclerAdapter

    private val nMap: MapsRepository by inject()
    private val userRepository: UserRepository by inject()
    private val pathDataRepository: PathDataRepository by inject()
    private val historyRepository: HistoryRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.request_activity)

        requestListView = findViewById(R.id.listview_requestdialog_list)
    }

    override fun onResume() {
        super.onResume()
        requestListView.adapter = pathDataRepository.adapter
        Log.e("onResume", "onResume")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}