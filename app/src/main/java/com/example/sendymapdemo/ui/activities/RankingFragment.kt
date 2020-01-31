package com.example.sendymapdemo.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.UserData
import com.example.sendymapdemo.ui.adapters.LeaderBoardAdapter
import com.example.sendymapdemo.viewmodel.RankingViewModel
import kotlinx.android.synthetic.main.ranking_activity.*
import kotlinx.android.synthetic.main.ranking_activity.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RankingFragment : Fragment(){
    private val rankingViewModel by viewModel<RankingViewModel>()

    private lateinit var leaderBoardLayoutManager: LinearLayoutManager
    private lateinit var adapter: LeaderBoardAdapter
    private var newUserDataList: List<UserData> ?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rankingViewModel.getAllUserData()
        subscribe()
        val view:View=inflater.inflate(R.layout.ranking_activity,container,false)
        view.back_button_ranking.setOnClickListener {
            view.findNavController().navigate(R.id.action_rankingActivity_to_mapsActivity)
        }
        return view
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
        leaderBoardLayoutManager = LinearLayoutManager(this.context)
        leaderBoard_recyclerList.adapter = adapter
        leaderBoard_recyclerList.layoutManager = leaderBoardLayoutManager
        adapter.notifyDataSetChanged()
    }
}