package com.example.sendymapdemo

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView

class loadingActivity(context : Context) : Dialog(context){
    private var c: Context? = null
    init{
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCanceledOnTouchOutside(false)
        c = context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loading_activity)
        val logo = findViewById<ImageView>(R.id.loadingIcon)
        val animation : Animation = AnimationUtils.loadAnimation(c,R.anim.loading)
        logo.animation = animation
    }
}