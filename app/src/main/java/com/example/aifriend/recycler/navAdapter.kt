package com.example.aifriend.recycler

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.ChatRoomActivity
import com.example.aifriend.MyApplication
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.userData
import com.example.aifriend.databinding.ViewNavdataBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class navAdapter(val context: Context, val itemList: List<userData>): RecyclerView.Adapter<navAdapter.MyViewHolder>() {
    private var fireStore: FirebaseFirestore? = null
    private var userName: String = ""   // 상대방 이름
    private var userEmail: String = ""
    private var userUid: String = ""
    inner class MyViewHolder(val binding: ViewNavdataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data: userData){
            binding.NameView.text = data.name
            binding.emailView.text = data.email

            userName = data.name.toString()
            userEmail = data.email.toString()
            userUid = data.uid.toString()
        }
        init {
            binding.textBtn.setOnClickListener {
                createChat()
                // 채팅방 이동
            } //채팅시작
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): navAdapter.MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewNavdataBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: navAdapter.MyViewHolder, position: Int) {
        val data = itemList.get(position)
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun createChat() {
        /**
         * 채팅방 중복 생성 되는 오류 해결하기
         *
         */
        var myUid = Firebase.auth.currentUser?.uid.toString()     // uid 받아오기
        var myEmail = MyApplication.email
        var myName = myEmail?.split('@')?.get(0)
        if (myName != null) {
            Log.d("tage", myName)
        }

        fireStore = FirebaseFirestore.getInstance()
        val newChat = fireStore?.collection("ChatRoomList")?.document()   // ChatRoomList 컬렉션에 문서생성 (문서 id는 랜덤으로 생성)

        val docKey = "ChatRoomList/" + newChat?.id    // 문서 id 받아오기
        val names = arrayListOf<String>(myName!!) // name : AI 로 설정 - 채팅방 리스트에 AI 라고 뜨도록
        names.add(userName)
        val users = arrayListOf<String>(myUid!!)  //  사용자 uid 받아옴 - 각 사용자마다 문서가 생성되므로
        users.add(userUid)

        // hashMap data 초기화
        val data = hashMapOf(
            "key" to docKey,
            "lastChat" to "대화를 시작해보세요.",
            "name" to names,
            "uid" to users,
        )
        // 데이터 추가
        newChat?.set(data)
        val intent = Intent(context, ChatRoomActivity::class.java)
        intent.putExtra("destinationUid", docKey)
        context?.startActivity(intent)
    }

//    private fun isChat():Boolean {
//        var uid = Firebase.auth.currentUser?.uid
//        fireStore?.collection("ChatRoomList")
//            ?.whereEqualTo("uid", uid!!)
//            ?.addSnapshotListener { value, error ->
//
//            }
//    }

}