package com.example.aifriend

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.aifriend.databinding.FragmentTab3Binding

class Tab3 : Fragment() {

    lateinit var binding: FragmentTab3Binding

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTab3Binding.inflate(inflater, container, false)
        setUpView()

        return binding.root
    }

    private fun setUpView(){
        //여기서 탭3 내용 작성
        //로그아웃
        binding.logoutBtn.setOnClickListener {
            MyApplication.auth.signOut()
            MyApplication.email=null
            startActivity(Intent(activity, MainActivity::class.java))
        }
    }
}