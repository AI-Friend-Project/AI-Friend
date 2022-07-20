package com.example.aifriend

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import com.example.aifriend.databinding.ActivityJoinBinding
import com.example.aifriend.databinding.ActivityLoginBinding
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity(){
    lateinit var binding: ActivityLoginBinding
    lateinit var binding2: ActivityJoinBinding
    //뒤로가기 종료
    var mBackWait:Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        binding2 = ActivityJoinBinding.inflate(layoutInflater)

        setContentView(binding.root)

        //회원가입 버튼 누르면 회원가입하는 창으로 이동, 내용 입력 후 버튼 누르면 메일 전송
        //내용 입력 유무 확인하는 기능 추가하기
        binding.joinBtn.setOnClickListener {
            setContentView(binding2.root)
            binding2.joinBtn.setOnClickListener {
                val email: String = binding2.idEditView.text.toString()
                val password: String = binding2.pwEditView.text.toString()

                MyApplication.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        binding2.idEditView.text.clear()
                        binding2.pwEditView.text.clear()
                        if (task.isSuccessful) {
                            MyApplication.auth.currentUser?.sendEmailVerification()
                                ?.addOnCompleteListener { sendTask ->
                                    if(sendTask.isSuccessful){
                                        Toast.makeText(baseContext, "회원가입에 성공하였습니다. 전송된 메일을 확인해 주세요.",
                                            Toast.LENGTH_SHORT).show()
                                        //파이어스토어 문서 생성
                                        documentCreate(email)
                                        setContentView(binding.root) //회원가입 후 로그인 화면으로 이동
                                    }else {
                                        Toast.makeText(baseContext, "메일 전송 실패",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(baseContext, "회원가입 실패",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
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
    private fun documentCreate(email: String){
        var temp = email.split("@") //이메일 아이디 부분만 따오기
        val userData = mapOf(
            "email" to email,
            "name" to temp[0]
        )
        MyApplication.db.collection("user").document(email)
            .set(userData)//user collection에 user 정보 저장

        //회원가입 시 공통 관심사로 "AI친구" 추가
        //user 컬렉션에 해당 user doc에서 하위 컬렉션 doc으로 저장
        val favData = mapOf(
            "favorite" to "AI친구"
        )
        MyApplication.db.collection("user").document(email)
            .collection("favorite").document("AI친구").set(favData)

        //fav 컬렉션의 해당 관심사 문서 안 필드배열에 사용자 이메일 추가
        MyApplication.db.collection("fav").document("AI친구")
            .update("users", FieldValue.arrayUnion(email))
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

}