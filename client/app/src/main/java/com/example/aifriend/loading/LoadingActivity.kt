package com.example.aifriend.loading

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.aifriend.R

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        startLoading();
    }
    private fun startLoading() {
        val handler = Handler()
        handler.postDelayed({ finish() }, 2000)
    }
}