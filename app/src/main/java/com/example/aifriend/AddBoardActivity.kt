package com.example.aifriend

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aifriend.databinding.ActivityAddBoardBinding
import com.google.firebase.firestore.FieldValue

class AddBoardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBoardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBoardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(binding) {
            save.setOnClickListener {
                saveBoardData()
            }
            cancel.setOnClickListener {
                finish()
            }
        }
    }

    private fun saveBoardData() {
        if (!binding.title.text.isNullOrEmpty() && !binding.content.text.isNullOrEmpty()) {
            intent.getStringExtra("favorite")?.let {
                val boardData = BoardData(
                    title = binding.title.text.toString(),
                    content = binding.content.text.toString(),
                    name = MyApplication.email!!
                )
                setBoardData(it, boardData)
            }
        } else {
            Toast.makeText(this, "타이틀과 내용을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setBoardData(favorite: String, data: BoardData) {
        MyApplication.db.collection("Board").document(favorite)
            .update("list", FieldValue.arrayUnion(data)).addOnSuccessListener {
                Toast.makeText(this, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                setResult(2000)
                finish()
            }
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