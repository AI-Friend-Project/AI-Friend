package com.example.aifriend.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.R
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.OtherUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class ChatRoomAdapter: RecyclerView.Adapter<ChatRoomAdapter.MessageViewHolder>() {
    private val comments = ArrayList<ChatData.Comment>()
    private var otherUser : OtherUser? = null
    var fireStore: FirebaseFirestore? = null
    private var uid : String? = Firebase.auth.currentUser?.uid.toString()

    init {
        fireStore = FirebaseFirestore.getInstance()

        fireStore?.collection("users")
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // ArrayList 비워줌
                comments.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject<ChatData.Comment>()
                    comments.add(item!!)
                }
            }
        notifyDataSetChanged()

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msgbox, parent, false))
    }

    //뷰 홀더 설정
    inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val msgTextView: TextView = view.findViewById(R.id.msgTextView)
        val timeTextView: TextView = view.findViewById(R.id.timeTextView)
        val senderMsgTextView: TextView = view.findViewById(R.id.senderMsgTextView)
        val senderTimeTextView: TextView = view.findViewById(R.id.senderTimeTextView)
        val sender: RelativeLayout = view.findViewById(R.id.sender)
        val receiver: RelativeLayout = view.findViewById(R.id.receiver)

    }

    //receiver 뷰 홀더 설정


    @SuppressLint("WrongConstant")
    @RequiresApi(31)
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        if(uid == comments[position].uid) {
            holder.sender.visibility = View.VISIBLE
            holder.receiver.visibility = View.INVISIBLE
            holder.senderMsgTextView.text = comments[position].message
            holder.senderTimeTextView.text = comments[position].time
        }
        else {
            holder.sender.visibility = View.INVISIBLE
            holder.receiver.visibility = View.VISIBLE
            holder.nameTextView.text = comments[position].uid
            holder.msgTextView.text = comments[position].message
            holder.timeTextView.text = comments[position].time
        }

    }

    override fun getItemCount(): Int {
        return comments.size
    }

}