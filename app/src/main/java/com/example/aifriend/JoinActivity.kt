package com.example.aifriend

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.aifriend.databinding.ActivityJoinBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class JoinActivity: AppCompatActivity() {
    lateinit var binding: ActivityJoinBinding
    private var uid : String? = null
    private var fireStore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.joinToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "회원가입"

        binding.joinBtn.setOnClickListener {
            val email: String = binding.idEditView.text.toString()
            val password: String = binding.pwEditView.text.toString()

            MyApplication.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.idEditView.text.clear()
                    binding.pwEditView.text.clear()
                    if (task.isSuccessful) {
                        MyApplication.auth.currentUser?.sendEmailVerification()
                            ?.addOnCompleteListener { sendTask ->
                                if(sendTask.isSuccessful){
                                    setAI()     // 회원가입 시 AI 채팅방 생성
                                    Toast.makeText(baseContext, "회원가입에 성공하였습니다. 전송된 메일을 확인해 주세요.",
                                        Toast.LENGTH_SHORT).show()
                                    //파이어스토어 문서 생성
                                    documentCreate(email)
                                    notiCreate(email)
                                    finish() //회원가입 후 로그인 화면으로 이동
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

    private fun documentCreate(email: String){
        var temp = email.split("@") //이메일 아이디 부분만 따오기
        uid = Firebase.auth.currentUser?.uid.toString()     // uid 받아오기

        val userData = mapOf(
            "email" to email,
            "name" to temp[0],
            "uid" to uid
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

        //user 컬렉션 안 사용자 문서에 fav array 추가
        MyApplication.db.collection("user").document(email)
            .update("fav", FieldValue.arrayUnion("AI친구"))

        MyApplication.db.collection("user").document(email)
            .update("friends", FieldValue.arrayUnion(""))

        MyApplication.db.collection("user").document(email)
            .update("request", FieldValue.arrayUnion(""))
    }

    private fun notiCreate(email : String){
        var currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
        val nowTime = currentTime.format(formatter)
        var message = "알림 메세지는 이 곳에 표시됩니다."
        var type = "system"
        val notification = mapOf(
            "type" to type,
            "content" to message,
            "time" to nowTime.toString()
        )
        MyApplication.db.collection("user").document(email)
            .collection("notification").add(notification)
    }

    /**
     * 각 유저마다 AI 채팅방 생성
     */

    private fun setAI(){
        uid = Firebase.auth.currentUser?.uid.toString()     // uid 받아오기
        fireStore = FirebaseFirestore.getInstance()
        val newChat = fireStore?.collection("AIChat")?.document()   // AI Chat 컬렉션에 문서생성 (문서 id는 랜덤으로 생성)

        val docKey = "AIChat/" + newChat?.id    // 문서 id 받아오기
        val names = arrayListOf<String>("AI", "AI") // name : AI 로 설정 - 채팅방 리스트에 AI 라고 뜨도록
        val users = arrayListOf<String>(uid!!)  //  사용자 uid 받아옴 - 각 사용자마다 문서가 생성되므로

        // hashMap data 초기화
        val data = hashMapOf(
            "key" to docKey,
            "lastChat" to "대화를 시작해보세요.",
            "name" to names,
            "uid" to users,
        )
        // 데이터 추가
        newChat?.set(data)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item!!.itemId) {
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}