package com.example.aifriend.recycler

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.MyApplication
import com.example.aifriend.R
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.ChatRoomData
import com.example.aifriend.data.OtherUser
import com.example.aifriend.data.ViewType
import com.example.aifriend.databinding.ReceiverMsgboxBinding
import com.example.aifriend.databinding.SenderMsgboxBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 채팅방 안에 리스트
 */
class ChatRoomAdapter(collectionPath: String,fieldPath: String): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var comments = ArrayList<ChatRoomData>()
    private var otherUser : OtherUser? = null
    private var fireStore: FirebaseFirestore? = null
    private var uid : String? = Firebase.auth.currentUser?.uid.toString()


    init {
        fireStore = FirebaseFirestore.getInstance()

        // 채팅 목록 가져오기
        fireStore?.collection(collectionPath)
            ?.document(fieldPath)
            ?.collection("Chats")
            ?.orderBy("time", Query.Direction.DESCENDING)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // ArrayList 비워줌
                comments.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject<ChatRoomData>()
                    comments.add(item!!)
                }
                notifyDataSetChanged()

            }


    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ViewType.SENDER) {
            // 사용자 쪽 채팅 레이아웃 설정
            SenderViewHolder(
                SenderMsgboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            // 상대방 쪽 채팅 레이아웃 설정
            ReceiverViewHolder(
                ReceiverMsgboxBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    //뷰 홀더 설정
    class SenderViewHolder(binding: SenderMsgboxBinding) : RecyclerView.ViewHolder(binding.root) {
        private var msgTextView = binding.msgTextView
        var timeTextView = binding.timeTextView

        fun bind(item: ChatRoomData) {
            msgTextView.text = item.message
            timeTextView.text = item.time
        }
    }

    class ReceiverViewHolder(binding: ReceiverMsgboxBinding) : RecyclerView.ViewHolder(binding.root) {
        private var msgTextView = binding.msgTextView
        private var timeTextView = binding.timeTextView
        private var nameTextView = binding.nameTextView

        fun bind(item: ChatRoomData) {
            msgTextView.text = item.message
            timeTextView.text = item.time
            nameTextView.text = item.name
            item.name?.let { Log.d("tag", it) }
        }


    }

    //receiver 뷰 홀더 설정


    @SuppressLint("WrongConstant")
    @RequiresApi(31)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        // 사용자 uid 와 채팅 uid 가 같을 경우 -> sender
        if(uid?.equals(comments[position].uid) == true) {
            // 사용자 쪽 채팅으로 설정
            (holder as SenderViewHolder).bind(comments[position])

        }
        else {  // 다를 경우 - 상대방 쪽 채팅으로 설정
            (holder as ReceiverViewHolder).bind(comments[position])

        }


    }

    override fun getItemCount(): Int {
        return comments.size
    }

    override fun getItemViewType(position: Int): Int {
        return if(uid?.equals(comments[position].uid) == true) {
            1
        } else {
            2
        }
    }

}

//fun calculateTime(time: Long): String {
//    val dateFormat = SimpleDateFormat("MM/dd. hh:mm")
//
//    return dateFormat.format(Date(time)).toString()
//}