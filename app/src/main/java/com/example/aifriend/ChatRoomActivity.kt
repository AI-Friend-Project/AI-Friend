package com.example.aifriend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.Utils.Constants
import com.example.aifriend.Utils.Constants.CHANNEL_ID
import com.example.aifriend.Utils.Constants.CHANNEL_NAME
import com.example.aifriend.data.ChatRoomData
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ActivityChatRoomBinding
import com.example.aifriend.recycler.ChatRoomAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

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
    private var name: String? = null
    private var aiChatRecyclerView: RecyclerView? = null
    private lateinit var keyboardVisibility: KeyboardVisibility


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val chatEditText = binding.chatEditText
        val chatSendButton = binding.chatSendButton

        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss",Locale.KOREA)
        val curTime = dateFormat.format(Date(time)).toString()
        destinationUid = intent.getStringExtra("destinationUid")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = binding.chatRoomActivityRecyclerView
        getName()

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
            val chat = ChatRoomData(name, chatEditText.text.toString(), curTime, uid)
//            recyclerView?.scrollToPosition(chatAdapter.itemCount - 1)
            //수정 작업 필요
            var chatDataMap = mutableMapOf<String,Any>()
            chatDataMap["key"] = destinationUid.toString()
            chatDataMap["lastChat"] = chatEditText.text.toString()
            chatDataMap["time"] = curTime
            name?.let { it1 -> sendNotification(it1, chatEditText.text.toString()) }
            //저장
            if(chatRoomUid == null) {
                fireStore.collection(collectionPath)
                    .document(fieldPathUid)
                    .update(chatDataMap)
                    .addOnSuccessListener {
                        chatRoom()

                        if (fieldPathUid != null) {
                            fireStore.collection(collectionPath)
                                .document(fieldPathUid)
                                .collection("Chats")
                                .add(chat)
                        }
                        chatEditText.text = null
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
        }
        chatRoom()

    }

    private fun chatRoom() {
        recyclerView?.layoutManager = LinearLayoutManager(this@ChatRoomActivity, LinearLayoutManager.VERTICAL, true)
        recyclerView?.adapter = chatAdapter


    }

    private fun getName() {
        var a: String? = null
        val userList = ArrayList<UserData>()

        fireStore?.collection("user")
            ?.whereEqualTo("uid", uid!!)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                // ArrayList 비워줌
                userList.clear()

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject<UserData>()
                    userList.add(item!!)
                }
                name = userList[0].name
            }
    }

    private fun sendNotification(title: String, message: String) {


        val intent = Intent(this, MainActivity::class.java)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val notificationID = Random()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationID.nextInt(), notification)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Channel Description"
            enableLights(true)
            lightColor = Color.GREEN
        }
        notificationManager.createNotificationChannel(channel)
    }





}

