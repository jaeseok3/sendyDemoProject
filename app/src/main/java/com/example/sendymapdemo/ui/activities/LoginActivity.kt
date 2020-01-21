package com.example.sendymapdemo.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.sendymapdemo.R
import com.example.sendymapdemo.model.repository.UserRepository
import org.koin.android.ext.android.inject

class LoginActivity : AppCompatActivity() {
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val userRepository: UserRepository by inject()

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        val mEditTextSearchKeyword = findViewById<EditText>(R.id.usrusr)
        val mButton:Button = findViewById(R.id.logiin)

        mButton.setOnClickListener {
            if(mEditTextSearchKeyword.text.toString().length in 1..10){
                val userID=mEditTextSearchKeyword.text.toString()
                Toast.makeText(this,"$userID 님 환영합니다",Toast.LENGTH_LONG).show()

                userRepository.getData(userID)
                val intent = Intent(applicationContext, MapsActivity::class.java)
                intent.putExtra("userID", "")
                startActivity(intent)

                finish()
            }
            else{
                Toast.makeText(this,"아이디를 10자 내로 입력하세요!",Toast.LENGTH_SHORT).show()
            }
        }
    }
}
