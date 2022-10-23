package com.example.aifriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.ChatData
import com.example.aifriend.recycler.AiChatAdapter
import com.example.aifriend.recycler.ChatAdapter

/**
 * 채팅방 리스트 프래그먼트
 */
class ChatFragment : Fragment() {

    private val chatList = ArrayList<ChatData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        val aiChatRecyclerView = view.findViewById<RecyclerView>(R.id.aiChatRecyclerView)
        val chatRecyclerView = view.findViewById<RecyclerView>(R.id.chatFragmentRecyclerView)

        // AI 채팅방, 상대 채팅방 띄우기
        aiChatRecyclerView.layoutManager = LinearLayoutManager(context)
        chatRecyclerView.layoutManager = LinearLayoutManager(context)

        aiChatRecyclerView.adapter = AiChatAdapter()
        chatRecyclerView.adapter = ChatAdapter()

        return view
    }
}