package com.example.aifriend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ActivityJoinBinding
import com.example.aifriend.databinding.ActivityLoginBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LoginActivity : AppCompatActivity(){
    lateinit var binding: ActivityLoginBinding
    lateinit var binding2: ActivityJoinBinding
    private val chatList = ArrayList<ChatData>()
    private var uid : String? = null
    private var fireStore: FirebaseFirestore? = null
    //뒤로가기 종료
    var mBackWait:Long = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding2 = ActivityJoinBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)

        binding.joinBtn.setOnClickListener {
            startActivity(Intent(this, JoinActivity::class.java))
        }

        //로그인
        binding.loginBtn.setOnClickListener {
            val email: String = binding.idEditView.text.toString()
            val password: String = binding.pwEditView.text.toString()

            MyApplication.auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.idEditView.text.clear()
                    binding.pwEditView.text.clear()
                    if (task.isSuccessful) {
                        if(MyApplication.checkAuth()){ //로그인 성공
                            checkToken(email)
                            MyApplication.email=email
                            finish()
                        }else {
                            //발송된 메일로 인증 확인을 안한경우
                            Toast.makeText(baseContext, "전송된 메일로 이메일 인증이 되지 않았습니다.",
                                Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "로그인 실패",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    //뒤로가기로 앱 종료
    override fun onBackPressed() {
        // 뒤로가기 버튼 클릭
        if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "뒤로가기를 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            finishAffinity()
        }
    }


    /**
     *  회원가입시 토큰 저장 ( 알림 기능 구현 시 필요 )
     */
    private fun getNewToken(myUserEmail:String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { task ->
            if(task.isNotEmpty()) {
                MyApplication.db.collection("user").document(myUserEmail)
                    .update("token", task)
            }
        }
    }

    private fun checkToken(myUserEmail: String) {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { task ->
            if(task.isNotEmpty()) {
                MyApplication.db.collection("user")?.document(myUserEmail)
                    ?.addSnapshotListener { documentSnapshot, _ ->
                        if (documentSnapshot == null) return@addSnapshotListener

                        val users = documentSnapshot.toObject<UserData>()

                        if (users?.uid != null) {
                            // 토큰이 변경되었을 경우 갱신
                            if (users.token != task) {
                                Log.d("tag", "profileLoad: 토큰 변경되었음.")
                                getNewToken(myUserEmail)
                            } else {
                                Log.d("tag", "profileLoad: 이미 동일한 토큰이 존재함.")
                            }
                            Log.d("tag", "uid != null")
                        } else {
                            Log.d("tag", "uid == null")
                        }
                    }
            }
        }
    }

}