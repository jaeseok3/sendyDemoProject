package com.example.sendymapdemo.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.example.sendymapdemo.R
import com.example.sendymapdemo.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : FragmentActivity() {
    private val loginViewModel by viewModel<LoginViewModel>()
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        login.setOnClickListener {
            val userID= useredit.text.toString()
            if(userID.length in 1..10){
                Toast.makeText(this,"$userID 님 환영합니다",Toast.LENGTH_LONG).show()

                loginViewModel.getData(userID)
                val intent = Intent(applicationContext, MapsActivity::class.java)
                startActivity(intent)

                finish()
            }
            else{
                Toast.makeText(this,"아이디를 10자 내로 입력하세요!",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
