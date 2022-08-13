package com.example.aifriend

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.ChatRoomData
import com.example.aifriend.databinding.ActivityChatRoomBinding
import com.example.aifriend.recycler.ChatRoomAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatRoomBinding
    private lateinit var chatAdapter: ChatRoomAdapter
    private val fireStore = FirebaseFirestore.getInstance()
    private var chatRoomUid : String? = null
    private var uid : String? = null
    private var destinationUid : String? = null
    private var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val chatEditText = findViewById<EditText>(R.id.chatEditText)
        val chatSendButton = findViewById<Button>(R.id.chatSendButton)

        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM/dd. hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        destinationUid = intent.getStringExtra("destinationUid")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = findViewById(R.id.chatRoomActivityRecyclerView)

        val fieldPathUid: String = destinationUid!!.split("/")[1]
        chatAdapter = ChatRoomAdapter(fieldPathUid)

        chatRoom(fieldPathUid)
        hideKeyboard()

        // 채팅 보내기
        chatSendButton.setOnClickListener {
            Log.i("qqq11", "클릭$destinationUid")
            val chat = ChatRoomData(uid, chatEditText.text.toString(), curTime, uid)
            recyclerView?.scrollToPosition(chatAdapter.itemCount - 1)
            //수정 작업 필요
            var chatDataMap = mutableMapOf<String,Any>()
            chatDataMap["key"] = destinationUid.toString()
            chatDataMap["lastChat"] = chatEditText.text.toString()
            Log.i("qqq11", "ff$fieldPathUid")

            //저장
           // if(chatRoomUid == null) {
                fireStore.collection("ChatRoomList")
                    .document(fieldPathUid)
                    .update(chatDataMap)
                    .addOnSuccessListener {

                       // Handler().postDelayed({
                        if (fieldPathUid != null) {
                            fireStore.collection("ChatRoomList")
                                .document(fieldPathUid)
                                .collection("Chats")
                                .add(chat)
                        }
                        chatEditText.text = null
                   // }, 100L)
                    chatRoom(fieldPathUid)

                }
            /*}
            else {
                if (fieldPathUid != null) {
                    fireStore.collection("ChatRoomList")
                        .document(fieldPathUid)
                        .collection("Chats")
                        .add(chat)
                }
                chatEditText.text = null
                Log.i("qqq11", "2채팅2")

            }*/
        }
        chatRoom(fieldPathUid)
    }
    private fun chatRoom(fieldPath: String) {
        recyclerView?.layoutManager = LinearLayoutManager(this@ChatRoomActivity, LinearLayoutManager.VERTICAL, false)
        recyclerView?.adapter = chatAdapter
        recyclerView?.scrollToPosition(chatAdapter.itemCount - 1)

    }
    private fun checkChatRoom(fieldPathUid: String) {
        Log.i("qqq11", "222")

        fireStore.collection("ChatRoomList")
            .document(fieldPathUid)
            .collection("Chats")
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject<ChatData>()
                    Log.i("qqq11", "$item")

                    if(destinationUid?.let { item?.uid!!.contains(it) } == true) {
                        chatRoomUid = item?.key

                       // recyclerView?.layoutManager = LinearLayoutManager(this)
                        //recyclerView?.adapter = ChatRoomAdapter()



                    }

                }
            }
    }
    fun hideKeyboard() {
        val imm2 = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm2.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    override fun onStart() {
        super.onStart()
        destinationUid = intent.getStringExtra("destinationUid")
        val fieldPathUid: String = destinationUid!!.split("/")[1]

        chatRoom(fieldPathUid)
        chatAdapter.notifyDataSetChanged()
    }

}
