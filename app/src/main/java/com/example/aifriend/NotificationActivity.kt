package com.example.aifriend

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.notificationData
import com.example.aifriend.databinding.NotificationBinding
import com.example.aifriend.recycler.notificationAdapter

@RequiresApi(Build.VERSION_CODES.O)
class NotificationActivity: AppCompatActivity() {
    lateinit var binding: NotificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
    private fun makeRecyclerView(){
        val email = MyApplication.email
        MyApplication.db.collection("user").document(email.toString())
            .collection("notification")
            .get()
            .addOnSuccessListener { result ->
                val itemList = mutableListOf<notificationData>()
                for (document in result){
                    val item = document.toObject(notificationData::class.java)
                    item.docId = document.id
                    itemList.add(item)
                }
                //var itemSort = itemList.sortByDescending { it.time}  as List<notificationData>
                binding.notiRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.notiRecyclerView.adapter = notificationAdapter(this, itemList)
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
     }
}