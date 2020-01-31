package com.example.sendymapdemo.ui.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import com.example.sendymapdemo.R

class MainFragment : AppCompatActivity() {

    private lateinit var navController: NavController
    val fragmentManager:FragmentManager=supportFragmentManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

//        navController = Navigation.findNavController(this,R.id.nav_host_fragment)
        println("main activity 시작")

//        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
//        transaction.add(R.id.loginActivity, LoginActivity())
//        transaction.addToBackStack(null)
//        transaction.commit()
    }
}