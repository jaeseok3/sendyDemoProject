package com.example.sendymapdemo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var mEditTextSearchKeyword = findViewById<EditText>(R.id.usrusr)
        var mButton:Button = findViewById(R.id.logiin)

        mButton.setOnClickListener {

            if(mEditTextSearchKeyword.text.toString().length in 1..10){
                Log.e("vaa",mEditTextSearchKeyword.text.toString())
                userID=mEditTextSearchKeyword.text.toString()
                Toast.makeText(this,"$userID 님 환영합니다",Toast.LENGTH_SHORT).show()
                login(userID!!)
                finish()

            }
            else{
                Toast.makeText(this,"아이디를 10자 내로 입력하세요!",Toast.LENGTH_SHORT).show()
            }

        }

    }
}
