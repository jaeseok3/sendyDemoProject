package com.example.sendymapdemo.ui.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.sendymapdemo.R
import com.example.sendymapdemo.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : Fragment() {
    private val loginViewModel by viewModel<LoginViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_login, container, false)

        login.setOnClickListener {
            val userID= useredit.text.toString()
            if(userID.length in 1..10){
                Toast.makeText(this.context,"$userID 님 환영합니다",Toast.LENGTH_LONG).show()

                loginViewModel.getData(userID)
                Navigation.findNavController(view).navigate(R.id.action_loginActivity_to_mapsActivity)
            }
            else{
                Toast.makeText(this.context,"아이디를 10자 내로 입력하세요!",Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
