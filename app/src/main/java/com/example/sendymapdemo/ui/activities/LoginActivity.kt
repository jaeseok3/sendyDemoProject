package com.example.sendymapdemo.ui.activities

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.sendymapdemo.R
import com.example.sendymapdemo.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : Fragment() {
    private val loginViewModel by viewModel<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var  view:View = inflater.inflate(R.layout.activity_login,container,false)

        view.login.setOnClickListener {
            val userID= useredit.text.toString()
            if(userID.length in 1..10){
                Toast.makeText(this.context,"$userID 님 환영합니다",Toast.LENGTH_LONG).show()
                val imm = this.context!!.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                loginViewModel.getData(userID)

//                val intent = Intent(applicationContext, MapsActivity::class.java)
//                startActivity(intent)
                view.findNavController().navigate(R.id.action_loginActivity_to_mapsActivity)
//                finish()
            }
            else{
                Toast.makeText(this.context,"아이디를 10자 내로 입력하세요!",Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_login)
//        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
//        }


    }
}
