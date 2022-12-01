package com.example.aifriend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.MoreBoardData
import com.example.aifriend.databinding.ActivityBoardViewBinding
import com.example.aifriend.recycler.BoardAdapter

@RequiresApi(Build.VERSION_CODES.O)
//게시글 더보기 액티비티
class BoardViewActivity: AppCompatActivity() {
    private lateinit var binding : ActivityBoardViewBinding
    private var favName = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBoardViewBinding.inflate (layoutInflater)
        setContentView(binding.root)

        favName = intent.getStringExtra("favName").toString()
        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = favName

        //게시물 목록
        //makeRecyclerView()
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
                binding.moreBoardRecyclerView.adapter = BoardAdapter(this, itemSort)
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun writing(){
        //인텐트 전달
        var intent = Intent(this, BoardWritingActivity::class.java)
        intent.putExtra("favName", favName.toString())
        startActivity(intent)
    }

    //글 등록 시 리사이클러뷰 업데이트
    override fun onResume() {
        super.onResume()
        makeRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add, menu)
        return true
    }

    //툴바 뒤로가기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home -> {
                finish()
            }
            R.id.menu_add -> {
                writing()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}