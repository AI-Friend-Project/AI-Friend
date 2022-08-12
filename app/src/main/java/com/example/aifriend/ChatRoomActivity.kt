package com.example.aifriend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.ChatRoomData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ChatRoomActivity : AppCompatActivity() {
    private val fireStore = FirebaseFirestore.getInstance()
    private var chatRoomUid : String? = null
    private var uid : String? = null
    private var destinationUid : String? = null
    private var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_room)
        val chatEditText = findViewById<EditText>(R.id.chatEditText)
        val chatSendButton = findViewById<Button>(R.id.chatSendButton)

        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM/dd. hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        destinationUid = intent.getStringExtra("destinationUid")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.chatRoomActivityRecyclerView)

        Log.i("qqq11", "클릭")

        chatSendButton.setOnClickListener {
            Log.i("qqq11", "클릭$destinationUid")
            val chat = ChatRoomData()
            val chatData = ChatData()

            chatData.key = destinationUid
            

            if(chatRoomUid == null) {
                fireStore.collection("ChatRoomList").add(chat)
            }

        }


    }
}