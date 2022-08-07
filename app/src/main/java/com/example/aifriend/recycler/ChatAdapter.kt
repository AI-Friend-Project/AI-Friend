package com.example.aifriend.recycler

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.ChatRoomActivity
import com.example.aifriend.R
import com.example.aifriend.data.ChatData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class ChatAdapter: RecyclerView.Adapter<ChatAdapter.UserViewHolder>() {
    private val chatList = ArrayList<ChatData>()
    private var uid : String? = null
    private var fireStore: FirebaseFirestore? = null
    private val destinationUsers: ArrayList<String> = arrayListOf()

    init {
        uid = Firebase.auth.currentUser?.uid.toString()

        fireStore = FirebaseFirestore.getInstance()

        fireStore?.collection("chatRooms")
            ?.orderBy("users/$uid")
            ?.whereEqualTo("uid", uid)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // ArrayList 비워줌
                chatList.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject<ChatData>()
                    chatList.add(item!!)
                }
            }
        notifyDataSetChanged()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))
    }

    //user 뷰 홀더 설정
    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val chatTitleTextView: TextView = view.findViewById(R.id.chatTitleTextView)
        private val chatMessageTextView: TextView = view.findViewById(R.id.chatMessageTextView)
    }



    @SuppressLint("WrongConstant")
    @RequiresApi(31)
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        var destinationUid: String? = null

        //채팅방 유저 체크
        for(user in chatList[position].users.keys) {
            if(user != uid) {
                destinationUid = user
                destinationUsers.add(destinationUid)
            }
        }

        //채팅창 선책 시 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatRoomActivity::class.java)
            intent.putExtra("destinationUid", destinationUsers[position])
            holder.itemView.context?.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }


}