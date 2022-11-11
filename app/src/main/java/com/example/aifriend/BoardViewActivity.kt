package com.example.aifriend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.MoreBoardData
import com.example.aifriend.databinding.ActivityBoardViewBinding
import com.example.aifriend.recycler.MoreBoardAdapter

//게시글 더보기 액티비티
class BoardViewActivity: AppCompatActivity() {
    private lateinit var binding : ActivityBoardViewBinding
    private var favName = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardViewBinding.inflate (layoutInflater)
        setContentView(binding.root)

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "게시글 더보기" //카테고리 이름으로 변경하기

        //인텐트 받아오기
        favName = intent.getStringExtra("favName").toString()
        //인텐트 전달
        var intent = Intent(this, BoardWritingActivity::class.java)
        intent.putExtra("favName", favName.toString())

        binding.addWriting.setOnClickListener{
            startActivity(intent)
        }
        //게시물 목록
        makeRecyclerView()
    }

    private fun makeRecyclerView(){
        MyApplication.db.collection("Board").document(favName.toString())
            .collection("post")
            .get()
            .addOnSuccessListener { result ->
                val itemList = mutableListOf<MoreBoardData>()
                for (document in result){
                    val item = document.toObject(MoreBoardData::class.java)
                    item.docId = document.id
                    itemList.add(item)
                }
                var itemSort = itemList.sortedByDescending { it.time }
                binding.moreBoardRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.moreBoardRecyclerView.adapter = MoreBoardAdapter(this, itemSort)
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    //글 등록 시 리사이클러뷰 업데이트
    override fun onResume() {
        super.onResume()
        makeRecyclerView()
    }

    //툴바 뒤로가기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}