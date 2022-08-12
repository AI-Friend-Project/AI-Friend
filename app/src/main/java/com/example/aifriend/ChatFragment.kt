package com.example.aifriend

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.ChatData
import com.example.aifriend.recycler.ChatAdapter

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
        val chatRecyclerView = view.findViewById<RecyclerView>(R.id.chatFragmentRecyclerView)
        chatRecyclerView.layoutManager = LinearLayoutManager(context)
        chatRecyclerView.adapter = ChatAdapter()

        return view
    }
}