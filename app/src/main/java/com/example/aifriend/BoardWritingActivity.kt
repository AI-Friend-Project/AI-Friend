package com.example.aifriend

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.aifriend.databinding.ActivityBoardWritingBinding
import com.google.firebase.storage.StorageReference
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class BoardWritingActivity : AppCompatActivity(){
    private lateinit var binding: ActivityBoardWritingBinding
    private var favName = ""
    lateinit var filePath: String

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
            R.id.menu_image -> {
                val intent = Intent(Intent.ACTION_PICK)
                intent.setDataAndType(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    "image/*"
                )
                startActivityForResult(intent, 10)
                return true
            }
            R.id.menu_writing -> {
                if (binding.writingTitle.text.isNotEmpty() &&
                    binding.writingContent.text.isNotEmpty()
                ) { //내용 확인
                    saveWriting()
                    //MoreBoardAdapter.notifyDataSetChanged()
                } else {
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

        val e = MyApplication.email.toString()
        val temp = e.split("@")
        var name = temp[0]


        Log.d("tag", name.toString())
        val data = mapOf(
            "email" to e,
            "name" to name,
            "title" to binding.writingTitle.text.toString(),
            "content" to binding.writingContent.text.toString(),
            "time" to nowTime.toString(),
            "favName" to favName
        )
        if (binding.writingImage.drawable != null){
            MyApplication.db.collection("Board").document(favName.toString())
                .collection("post")
                .add(data)
                .addOnSuccessListener {
                    uploadImage(it.id)
                    finish()
                }.addOnFailureListener {
                    Log.w("tag", "fail to save")
                }
        }else{
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

    private fun uploadImage(docId: String){
        val storage = MyApplication.storage
        val  storageRef: StorageReference = storage.reference
        val imgRef: StorageReference = storageRef.child("images/${docId}.jpg")
        var file = Uri.fromFile(File(filePath))
        imgRef.putFile(file)
            .addOnFailureListener {
                Log.d("tag","image upload fail")
            }.addOnSuccessListener {
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode===10 && resultCode=== Activity.RESULT_OK){
            binding.writingImage.visibility = View.VISIBLE
            Glide
                .with(getApplicationContext())
                .load(data?.data)
                .apply(RequestOptions().override(300, 300))
                .centerCrop()
                .into(binding.writingImage)


            val cursor = contentResolver.query(data?.data as Uri,
                arrayOf<String>(MediaStore.Images.Media.DATA), null, null, null);
            cursor?.moveToFirst().let {
                filePath=cursor?.getString(0) as String
            }
        }
    }
}