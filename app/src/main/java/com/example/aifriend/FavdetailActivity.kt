package com.example.aifriend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aifriend.databinding.ActivityFavdetailBinding

class FavdetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavdetailBinding

    override fun onCreate (savedInstanceState: Bundle?){
        binding = ActivityFavdetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val favName = intent.getStringExtra("favName")
        binding.favNameView.text = favName
    }
}