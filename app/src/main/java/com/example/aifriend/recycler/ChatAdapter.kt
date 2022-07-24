package com.example.aifriend.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.R
import com.example.aifriend.data.ChatData


class ChatAdapter(var user: String, var itemList: ArrayList<ChatData>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /*
    init {
    }
    */

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View?

        return when(viewType) {
            // sender 일 때 1 설정
            1 -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.sender_msgbox,
                    parent,
                    false
                )
                senderViewHolder(view)
            }
            // receiver 일 때 2 설정
            else -> {
                view = LayoutInflater.from(parent.context).inflate(
                    R.layout.receiver_msgbox,
                    parent,
                    false
                )
                receiverViewHolder(view)
            }
        }
    }

    //sender 뷰 홀더 설정
    inner class senderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val msgText: TextView = view.findViewById(R.id.msgTextView)
        private val timeText: TextView = view.findViewById(R.id.timeTextView)
        
        fun bind(item: ChatData) {
            msgText.text = item.message
            timeText.text = item.time.toString()
        }
    }

    //receiver 뷰 홀더 설정
    inner class receiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        
        private val nameText: TextView = view.findViewById(R.id.nameTextView)
        private val msgText: TextView = view.findViewById(R.id.msgTextView)
        private val timeText: TextView = view.findViewById(R.id.timeTextView)

        fun bind(item: ChatData) {
            nameText.text = item.name
            msgText.text = item.message
            timeText.text = item.time.toString()
        }
    }


    @SuppressLint("WrongConstant")
    @RequiresApi(31)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when(itemList[position].type) {
            1 -> {
                (holder as senderViewHolder).bind(itemList[position])
                holder.setIsRecyclable(false)
            }
            else -> {
                (holder as receiverViewHolder).bind(itemList[position])
                holder.setIsRecyclable(false)
            }
        }

    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return itemList[position].type
    }



}