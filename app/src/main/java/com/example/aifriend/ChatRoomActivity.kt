package com.example.aifriend

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.BuildConfig.IP_ADDRESS
import com.example.aifriend.BuildConfig.SERVER_PORT
import com.example.aifriend.Utils.Constants.FCM_MESSAGE_URL
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.ChatRoomData
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ActivityChatRoomBinding
import com.example.aifriend.recycler.ChatRoomAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val chatEditText = binding.chatEditText
        val chatSendButton = binding.chatSendButton

        val time = System.currentTimeMillis()
        var date = Date(time)

        destinationUid = intent.getStringExtra("destinationUid")
        val collectionPath : String = destinationUid!!.split("/")?.get(0)
        val fieldPathUid: String = destinationUid!!.split("/")?.get(1)
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = binding.chatRoomActivityRecyclerView
        getName()


        /**
         * 채팅 띄우기
         **/
        chatAdapter = ChatRoomAdapter(collectionPath,fieldPathUid)
        chatRoom()

        var docRef = MyApplication.db.collection(collectionPath).document(fieldPathUid)

        var checkList = arrayListOf<Int>()
        checkList.add(1)
        checkList.add(1)

        docRef.get().addOnSuccessListener {
            var item = it.toObject<ChatData>()
            if(item?.uid?.get(0) == uid) {
                checkList[0] = 1
                checkList[1] = item?.check?.get(1)!!
            } else {
                // uid[1] == 내 uid
                checkList[0] = item?.check?.get(0)!!
                checkList[1] = 1
            }
            var checks = hashMapOf(
                "check" to checkList
            )
            docRef.update(checks as Map<String, Any>).addOnSuccessListener {
                Log.d("tag", "채팅 확인")
            }.addOnFailureListener{
                Log.d("tag", "채팅 확인 실패")
            }
        }


//        var map_0 = mutableMapOf<String, Any>()
//        map_0["check_0"] = 0
//        var map_1 = mutableMapOf<String, Any>()
//        map_0["check_1"] = 0
//
//        MyApplication.db.collection("ChatRoomList").document(fieldPathUid)
//            .update(map_0).addOnSuccessListener {
//                Toast.makeText(this, "채팅 확인", Toast.LENGTH_SHORT).show()
//            }.addOnFailureListener{
//                Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
//            }


        /**
         *  채팅 보내기
         **/
        chatSendButton.setOnClickListener {
            val chat = ChatRoomData(name, chatEditText.text.toString(), date, uid)

            //수정 작업 필요
            var chatDataMap = mutableMapOf<String,Any>()
            chatDataMap["key"] = destinationUid.toString()
            chatDataMap["lastChat"] = chatEditText.text.toString()
            chatDataMap["time"] = date
            sendPostToFCM(destinationUid!!, uid!!, name, chatEditText.text.toString())
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
            // 채팅 보내면 상대에 1로 변경
            docRef.get().addOnSuccessListener {
                var item = it.toObject<ChatData>()
                if (item?.uid?.get(0) == uid) {
                    checkList[0] = item?.check?.get(0)!!    // 나
                    checkList[1] = 0   // 상대방
                } else {
                    // uid[1] == 내 uid
                    checkList[0] = 0   // 상대방
                    checkList[1] = item?.check?.get(1)!!    // 나
                }
                var checks = hashMapOf(
                    "check" to checkList
                )
                docRef.update(checks as Map<String, Any>).addOnSuccessListener {
                    Log.d("tag", "채팅 확인")
                }.addOnFailureListener{
                    Log.d("tag", "채팅 확인 실패")
                }
            }
//            var map_0 = mutableMapOf<String, Any>()
//            map_0["check_0"] = 1
//            var map_1 = mutableMapOf<String, Any>()
//            map_0["check_1"] = 1
//
//            MyApplication.db.collection("ChatRoomList").document(fieldPathUid)
//                .update(map_0).addOnSuccessListener {
//                    Toast.makeText(this, "채팅 확인", Toast.LENGTH_SHORT).show()
//                }.addOnFailureListener{
//                    Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
//                }
            if (collectionPath == "AIChat") {
                // socket 통신
                thread {
                    val socket = Socket(IP_ADDRESS, SERVER_PORT)
                    val outStream = socket.outputStream

                    val data = "AIchat" + uid!!
                    val charset = Charsets.UTF_8
                    outStream.write(data.toByteArray(charset))

                    socket.close()
                }
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

    private fun sendPostToFCM(destinationUid: String, myUid: String, pTitle: String?, pMessage: String?) {
        val collectionPath: String = destinationUid!!.split("/")?.get(0)
        val fieldPathUid: String = destinationUid!!.split("/")?.get(1)
        val docRef = fireStore.collection(collectionPath).document(fieldPathUid)
        var userUid: String = ""
        if (collectionPath != "AIChat") {
            docRef.get().addOnSuccessListener {
                Log.d("tag", it.data.toString())
                var item = it.toObject<ChatData>()
                if (item != null) {
                    userUid = if (item.uid?.get(0)?.equals(myUid) == true) {
                        item.uid?.get(1).toString()
                    } else {
                        item.uid?.get(0).toString()
                    }
                }
                if (userUid != null) {
                    var token: String = ""
                    fireStore.collection("user")
                        .whereEqualTo("uid", userUid)
                        .addSnapshotListener { value, error ->
                            for (snapshot in value!!.documents) {
                                var item = snapshot.toObject<UserData>()
                                token = item?.token.toString()
                            }

                            Thread(
                                Runnable {
                                    kotlin.run {
                                        try {
                                            val root = JSONObject()
                                            val notification = JSONObject()

                                            notification.put("title", pTitle)
                                            notification.put("body", pMessage)


                                            root.put(
                                                "data",
                                                notification
                                            ) // 여기서 data와 notification 두가지 중 설정하면 된다.
                                            root.put("to", token)

                                            val url = URL(FCM_MESSAGE_URL)!!
                                            val conn = url.openConnection() as HttpURLConnection
                                            conn.requestMethod = "POST"
                                            conn.doOutput = true
                                            conn.doInput = true
                                            conn.addRequestProperty(
                                                "Authorization",
                                                "key=${BuildConfig.FCM_SERVER_KEY}"
                                            ) //받아 온 서버키를 넣어주세요
                                            conn.setRequestProperty("Accept", "application/json")
                                            conn.setRequestProperty(
                                                "Content-type",
                                                "application/json"
                                            )

                                            val os = conn.outputStream
                                            os.write(root.toString().toByteArray(Charsets.UTF_8));

                                            os.flush();
                                            conn.responseCode
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }

                                }
                            ).start()

                        }
                }
            }

        }
    }

    override fun onResume() {
        super.onResume()
        val collectionPath : String = destinationUid!!.split("/")?.get(0)
        val fieldPathUid: String = destinationUid!!.split("/")?.get(1)
        var docRef = MyApplication.db.collection(collectionPath).document(fieldPathUid)

        docRef.get().addOnSuccessListener {
            var checkList = arrayListOf<Int>()
            checkList.add(1)
            checkList.add(1)
            var item = it.toObject<ChatData>()
            if(item?.uid?.get(0) == uid) {
                checkList[0] = 1
                checkList[1] = item?.check?.get(1)!!
            } else {
                // uid[1] == 내 uid
                checkList[0] = item?.check?.get(0)!!
                checkList[1] = 1
            }
            var checks = hashMapOf(
                "check" to checkList
            )
            docRef.update(checks as Map<String, Any>).addOnSuccessListener {
                Log.d("tag", "채팅 확인")
            }.addOnFailureListener{
                Log.d("tag", "채팅 확인 실패")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        val collectionPath : String = destinationUid!!.split("/")?.get(0)
        val fieldPathUid: String = destinationUid!!.split("/")?.get(1)
        var docRef = MyApplication.db.collection(collectionPath).document(fieldPathUid)

        docRef.get().addOnSuccessListener {
            var checkList = arrayListOf<Int>()
            checkList.add(1)
            checkList.add(1)
            var item = it.toObject<ChatData>()
            if(item?.uid?.get(0) == uid) {
                checkList[0] = 1
                checkList[1] = item?.check?.get(1)!!
            } else {
                // uid[1] == 내 uid
                checkList[0] = item?.check?.get(0)!!
                checkList[1] = 1
            }
            var checks = hashMapOf(
                "check" to checkList
            )
            docRef.update(checks as Map<String, Any>).addOnSuccessListener {
                Log.d("tag", "채팅 확인")
            }.addOnFailureListener{
                Log.d("tag", "채팅 확인 실패")
            }
        }
    }


}

