package com.example.aifriend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.aifriend.databinding.ActivityBoardWritingBinding
import com.example.aifriend.recycler.MoreBoardAdapter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class BoardWritingActivity : AppCompatActivity(){
    private lateinit var binding: ActivityBoardWritingBinding
    private var favName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardWritingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "글 등록하기"

        favName = intent.getStringExtra("favName").toString()

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_writing, menu)
        return true
    }
    //툴바 메뉴
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_writing -> {
                if (binding.writingTitle.text.isNotEmpty() &&
                        binding.writingContent.text.isNotEmpty()) { //내용 확인
                    saveWriting()
                    //MoreBoardAdapter.notifyDataSetChanged()
                }else {
                    Toast.makeText(this, "제목과 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                //파이어베이스 저장
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun saveWriting() {
        var currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
        val nowTime = currentTime.format(formatter)

        val data = mapOf(
            "email" to MyApplication.email,
            "title" to binding.writingTitle.text.toString(),
            "content" to binding.writingContent.text.toString(),
            "time" to nowTime.toString()
        )
        //파이어스토어 저장
        MyApplication.db.collection("Board").document(favName.toString())
            .collection("post")
            .add(data)
            .addOnSuccessListener {
                finish()
            }.addOnFailureListener {
                Log.w("tag", "fail to save")
            }

    }
}