package com.example.aifriend

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.ChatRoomData
import com.example.aifriend.databinding.ActivityChatRoomBinding
import com.example.aifriend.recycler.ChatRoomAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.typeOf

/**
 * 채팅 화면 프래그먼트
 */
class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatAdapter: ChatRoomAdapter
    private val fireStore = FirebaseFirestore.getInstance()
    private var chatRoomUid : String? = null
    private var uid : String? = null
    private var destinationUid : String? = null
    private var recyclerView: RecyclerView? = null
    private var aiChatRecyclerView: RecyclerView? = null
    private lateinit var keyboardVisibility: KeyboardVisibility


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val chatEditText = binding.chatEditText
        val chatSendButton = binding.chatSendButton

        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM/dd. hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        destinationUid = intent.getStringExtra("destinationUid")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = binding.chatRoomActivityRecyclerView

        val collectionPath : String = destinationUid!!.split("/")?.get(0)
        val fieldPathUid: String = destinationUid!!.split("/")?.get(1)

        /**
         * 채팅 띄우기
         **/
        chatAdapter = ChatRoomAdapter(collectionPath,fieldPathUid)
        chatRoom()

        /**
         *  채팅 보내기
         **/
        chatSendButton.setOnClickListener {
            val chat = ChatRoomData(uid, chatEditText.text.toString(), curTime, uid)
            recyclerView?.scrollToPosition(chatAdapter.itemCount - 1)
            //수정 작업 필요
            var chatDataMap = mutableMapOf<String,Any>()
            chatDataMap["key"] = destinationUid.toString()
            chatDataMap["lastChat"] = chatEditText.text.toString()

            //저장
            if(chatRoomUid == null) {
                fireStore.collection(collectionPath)
                    .document(fieldPathUid)
                    .update(chatDataMap)
                    .addOnSuccessListener {
                        chatRoom()

                        // Handler().postDelayed({
                        if (fieldPathUid != null) {
                            fireStore.collection(collectionPath)
                                .document(fieldPathUid)
                                .collection("Chats")
                                .add(chat)
                        }
                        chatEditText.text = null
                        // }, 100L)
                    }
            }
            else {
                if (fieldPathUid != null) {
                    fireStore.collection(collectionPath)
                        .document(fieldPathUid)
                        .collection("Chats")
                        .add(chat)
                }
                chatEditText.text = null
            }
            keyBoard()
        }
        chatRoom()

    }

    private fun chatRoom() {
        recyclerView?.layoutManager = LinearLayoutManager(this@ChatRoomActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView?.adapter = chatAdapter
        keyBoard()
        scroll()

    }

    private fun keyBoard() {
        keyboardVisibility = KeyboardVisibility(window,
            onShowKeyboard = { keyboardHeight ->
                recyclerView?.run {
                    smoothScrollBy(scrollX,scrollY + keyboardHeight + 20)
                }
            })
    }

    private fun scroll() {
        Handler().postDelayed({
            recyclerView?.scrollToPosition(chatAdapter.itemCount - 1)
        }, 200)
    }



}