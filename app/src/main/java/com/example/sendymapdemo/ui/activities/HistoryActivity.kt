package com.example.sendymapdemo.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sendymapdemo.R
import com.example.sendymapdemo.dataclass.HistoryData
import com.example.sendymapdemo.ui.adapters.HistoryListAdapter
import com.example.sendymapdemo.viewmodel.HistoryViewModel
import kotlinx.android.synthetic.main.history_activiry.*
import kotlinx.android.synthetic.main.history_activiry.view.*
import kotlinx.android.synthetic.main.ranking_activity.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryActivity : Fragment(){
    private val historyViewModel by viewModel<HistoryViewModel>()

    private lateinit var historylayoutManager: LinearLayoutManager
    private lateinit var adapter: HistoryListAdapter
    private var newHistoryList : List<HistoryData> ?= null
    override fun onDestroyView() {
        super.onDestroyView()
        super.onPause()
        val fragmentManager: FragmentManager = activity!!.supportFragmentManager
        fragmentManager.beginTransaction().remove(this).commit()
        fragmentManager.popBackStack()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.history_activiry)
        historyViewModel.getHistory()
        subscribe()


    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view: View =inflater.inflate(R.layout.history_activiry,container,false)

        view.back_button_history.setOnClickListener {
            val fragmentManager: FragmentManager = activity!!.supportFragmentManager
            fragmentManager.beginTransaction().remove(this@HistoryActivity).commit()
            fragmentManager.popBackStack()
            println("ranking click")
        }
        return view
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
        historylayoutManager = LinearLayoutManager(this.context)

        historyRecyclerList.adapter = adapter
        historyRecyclerList.layoutManager = historylayoutManager
    }
}