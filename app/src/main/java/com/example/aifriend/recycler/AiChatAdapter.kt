package com.example.aifriend.recycler

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.*
import com.example.aifriend.BuildConfig.USER_DELIMITER
import com.example.aifriend.Utils.Constants
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.CheckData
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

// AI와 채팅
@SuppressLint("NotifyDataSetChanged")
class AiChatAdapter : RecyclerView.Adapter<AiChatAdapter.ViewHolder>() {
    private val chatList = ArrayList<ChatData>()
    private var uid = Firebase.auth.currentUser?.uid.toString()
    private var fireStore: FirebaseFirestore? = null
    private val destinationUsers: ArrayList<String> = arrayListOf()
    var count : Int = 0
    private lateinit var mContext :Context

    init {
        fireStore = FirebaseFirestore.getInstance()

        fireStore?.collection("AIChat")
            ?.whereArrayContains("uid", uid!!)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // ArrayList 비워줌
                chatList.clear()
                Log.i("qqq11", querySnapshot!!.documents.toString())

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject<ChatData>()
                    item?.key = "AIChat/" + snapshot.id
                    chatList.add(item!!)
                }

                notifyItemChanged(0)
            }

        // DiffUtil 로 갱신 해보기

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context

        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false))
    }

    //user 뷰 홀더 설정
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val chatTitleTextView: TextView = view.findViewById(R.id.chatTitleTextView)
        val chatMessageTextView: TextView = view.findViewById(R.id.chatMessageTextView)
        val receivedChatNotificationIcon : ImageView = view.findViewById(R.id.receivedChatNotificationIcon)
    }



    @SuppressLint("WrongConstant")
    @RequiresApi(31)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatInfo: ArrayList<String> = ArrayList()

        chatList[position].key?.let { chatInfo.add(it) }
        chatList[position].name?.get(1)?.let { chatInfo.add(it) }
        // AI
        holder.chatTitleTextView.text = chatList[position].name?.get(1)
        var lastChatTmp = chatList[position].lastChat?.split(USER_DELIMITER)
        if(lastChatTmp?.get(0) == "user") {
            // 유저 채팅
            holder.chatMessageTextView.text = lastChatTmp[1]
        } else {
            // AI 채팅
            holder.chatMessageTextView.text = chatList[position].lastChat
        }
//        chatList[position]?.check?.get(0) == 0 &&


        if(chatList[position].check?.get(0) == 0 ) {
            holder.receivedChatNotificationIcon.visibility = View.VISIBLE
        } else {
            holder.receivedChatNotificationIcon.visibility = View.INVISIBLE
        }

        //채팅창 선책 시 이동
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatRoomActivity::class.java)
            intent.putExtra("chatInfo", chatInfo)
            holder.itemView.context?.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return chatList.size
    }

}