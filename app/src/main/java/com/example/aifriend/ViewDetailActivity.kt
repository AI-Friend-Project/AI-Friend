package com.example.aifriend

import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.aifriend.databinding.ActivityViewDetailBinding
import com.google.firebase.firestore.FieldValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class ViewDetailActivity:AppCompatActivity() {
    private lateinit var binding: ActivityViewDetailBinding
    private var favName:String? = null
    private var docId:String? = null
    private var writerEmail:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityViewDetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val title = intent.getStringExtra("title")
        val name = intent.getStringExtra("name")
        val date = intent.getStringExtra("date")
        val content = intent.getStringExtra("content")
        docId = intent.getStringExtra("docId")
        favName = intent.getStringExtra("favName")
        writerEmail = intent.getStringExtra("email")

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = favName.toString()

        binding.viewDetailTitle.text = title
        binding.viewDetailName.text = name
        binding.viewDetailDate.text = date
        binding.viewDetailContent.text = content

        //이미지 불러오기
        val imgRef = MyApplication.storage.reference.child("images/${docId}.jpg")
        imgRef.downloadUrl.addOnSuccessListener { Uri->
            val imgURL = Uri.toString()
            binding.viewDetailImage.visibility = View.VISIBLE
            Glide.with(this).load(imgURL).override(300,300).into(binding.viewDetailImage)
            Log.d("tag","success")
        }

        val popUpAdd = AlertDialog.Builder(this)
        popUpAdd
            .setTitle("친구를 신청하시겠습니까?")
            .setPositiveButton("네",
                DialogInterface.OnClickListener{ dialogInterface, i ->
                    request(writerEmail.toString())
                })
            .setNegativeButton("아니오",
                DialogInterface.OnClickListener{ dialogInterface, i ->

                })

        val popUpWait = AlertDialog.Builder(this)
        popUpWait
            .setTitle("친구 신청 수락을 기다리고 있습니다.")
            .setPositiveButton("확인",
                DialogInterface.OnClickListener{ dialogInterface, i ->
                })

        val popUpChat = AlertDialog.Builder(this)
        popUpChat
            .setTitle("채팅을 시작하시겠습니까?")
            .setPositiveButton("네",
                DialogInterface.OnClickListener{ dialogInterface, i ->
                })
            .setNegativeButton("아니오",
                DialogInterface.OnClickListener { dialogInterface, i ->
                })

        //내가 쓴 글일땐 친구 추가 버튼 비활성화
        if (writerEmail.toString() == MyApplication.email){
            binding.addFriendBtn.isEnabled = false
        }

        binding.addFriendBtn.setOnClickListener {
            MyApplication.db.collection("user").document(MyApplication.email.toString())
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friends = document.get("friends") as List<String>
                        val request = document.get("request") as List<String>
                        if (friends.contains(writerEmail.toString())) {
                            popUpChat.show()
                        } else if (request.contains(writerEmail.toString())) {
                            popUpWait.show()
                        } else popUpAdd.show()
                    }
                }
        }
    }

    private fun request(email: String){
        MyApplication.db.collection("user").document(MyApplication.email.toString())
            .update("request", FieldValue.arrayUnion(email))
        Toast.makeText(this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
        Log.d("tag", email.toString())
        //친구 요청 알림 (알림 - 친구 요청, 시스템 알림)
        var currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
        val nowTime = currentTime.format(formatter)
        var message = "${MyApplication.email}님이 친구 요청을 보냈습니다."
        val notification = mapOf(
            "type" to "request",
            "content" to message,
            "time" to nowTime.toString(),
            "requestEmail" to MyApplication.email
        )
        MyApplication.db.collection("user").document(email.toString())
            .collection("notification").add(notification)
        //요청 친구 목록에 추가
        MyApplication.db.collection("user").document(MyApplication.email.toString())
            .update("request", FieldValue.arrayUnion(email))
        MyApplication.db.collection("user").document(email.toString())
            .update("request", FieldValue.arrayUnion(MyApplication.email.toString()))
        //notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_board_view, menu)
        var m_delete = menu?.findItem(R.id.menu_delete)
        if (writerEmail == MyApplication.email){
            m_delete?.setVisible(true)
        }else
            m_delete?.setVisible(false)
        return true
    }
    //툴바 메뉴
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            R.id.menu_delete -> {
                if(writerEmail == MyApplication.email){
                    MyApplication.db.collection("Board").document(favName.toString())
                        .collection("post").document(docId.toString())
                        .delete().addOnSuccessListener {
                            Toast.makeText(this, "삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{
                            Toast.makeText(this, "삭제에 실패하였습니다.", Toast.LENGTH_SHORT).show()
                        }
                    finish()
                }
                else
                    Toast.makeText(this, "본인이 작성한 글만 삭제할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}