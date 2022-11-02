package com.example.aifriend.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.notificationData
import com.example.aifriend.data.userData
import com.example.aifriend.databinding.ViewNavdataBinding
import com.example.aifriend.databinding.ViewNotidataBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class navAdapter(val context: Context, val itemList: List<userData>): RecyclerView.Adapter<navAdapter.MyViewHolder>() {
    private var fireStore: FirebaseFirestore? = null
    private var userName: String = ""   // 상대방 이름
    inner class MyViewHolder(val binding: ViewNavdataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data: userData){
            binding.NameView.text = data.name
            binding.emailView.text = data.email

            userName = data.name.toString()
        }
        init {
            binding.textBtn.setOnClickListener {
                createChat()
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
         * key: chatRoomList/ + document id
         * lastChat
         * name[2]
         * uid[2]
         *
         * 상대 uid, 내 이름 받아와서 데이터 넣어주기**
         *
         */
        var uid = Firebase.auth.currentUser?.uid.toString()     // uid 받아오기
        fireStore = FirebaseFirestore.getInstance()
        val newChat = fireStore?.collection("ChatRoomList")?.document()   // ChatRoomList 컬렉션에 문서생성 (문서 id는 랜덤으로 생성)

        val docKey = "ChatRoomList/" + newChat?.id    // 문서 id 받아오기
        val names = arrayListOf<String>(userName, "AI") // name : AI 로 설정 - 채팅방 리스트에 AI 라고 뜨도록
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
}