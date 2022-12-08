package com.example.aifriend.recycler

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.ChatRoomActivity
import com.example.aifriend.MyApplication
import com.example.aifriend.data.ChatData
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ViewNavdataBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class NavAdapter(val context: Context, val itemList: List<UserData>): RecyclerView.Adapter<NavAdapter.MyViewHolder>() {
    private var fireStore: FirebaseFirestore? = null
    private var userName: String = ""   // 상대방 이름
    private var userEmail: String = ""
    private var userUid: String = ""
    var chatList = ArrayList<ChatData>()
    private var myUid = Firebase.auth.currentUser?.uid.toString()     // uid 받아오기
    var tmp = 0

    inner class MyViewHolder(val binding: ViewNavdataBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: UserData) {
            binding.NameView.text = data.name
            binding.emailView.text = data.email

            userName = data.name.toString()
            userEmail = data.email.toString()
            userUid = data.uid.toString()
        }

        init {
            binding.textBtn.setOnClickListener {
                createChat()
                // 채팅방 이동
            } //채팅시작
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavAdapter.MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewNavdataBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: NavAdapter.MyViewHolder, position: Int) {
        val data = itemList[position]
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    private fun createChat() {
        /**
         * 채팅방 중복 생성 되는 오류 해결하기
         * 시간 오류
         */
        var myEmail = MyApplication.email
        var myName = myEmail?.split('@')?.get(0)

        fireStore = FirebaseFirestore.getInstance()
        val users = arrayListOf<String>(myUid!!)  //  사용자 uid 받아옴 - 각 사용자마다 문서가 생성되므로
        users.add(userUid)

        // 여러번 실행되는거 고치기
        val intent = Intent(context, ChatRoomActivity::class.java)
        checkChat { Log.d("tag", "Chat: " +chatList)
            if (chatList.isNotEmpty()) {
                if(tmp == 0) {
                    Log.d("tag", "is Not Empty -------------")
                    intent.putExtra("destinationUid", chatList[0].key)
                    context?.startActivity(intent)
                } else {
                    Log.d("tag", "is Not Empty2 -------------")
                }
            } else {
                if(tmp == 0) {
                    Log.d("tag", "is Empty -------------")
                    val newChat = fireStore?.collection("ChatRoomList")
                        ?.document()   // ChatRoomList 컬렉션에 문서생성 (문서 id는 랜덤으로 생성)

                    val docKey = "ChatRoomList/" + newChat?.id    // 문서 id 받아오기
                    val names = arrayListOf<String>(myName!!) // name : AI 로 설정 - 채팅방 리스트에 AI 라고 뜨도록
                    names.add(userName)
                    val checkList = arrayListOf<Int>()
                    checkList?.add(1)
                    checkList?.add(1)
                    // hashMap data 초기화
                    val data = hashMapOf(
                        "key" to docKey,
                        "lastChat" to "대화를 시작해보세요.",
                        "name" to names,
                        "check" to checkList,
                        "uid" to users,
                    )
                    // 데이터 추가
                    newChat?.set(data)?.addOnSuccessListener {
                        intent.putExtra("destinationUid", docKey)
                        context?.startActivity(intent)
                    }
                } else {
                    Log.d("tag", "is Empty3 -------------")
                }
            }
        }
    }


    /**
     * callback 함수를 이용한 내 채팅, 사용자 채팅 가져오기
     */
    private fun checkChat(callback: (ArrayList<ChatData>) -> Unit) {
        fireStore?.collection("ChatRoomList")
            ?.whereArrayContains("uid", myUid!!)
            ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                chatList.clear()
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject<ChatData>()
                    item?.key = "ChatRoomList/" + snapshot.id
                    if(item?.uid?.get(0).equals(userUid)) {
                        chatList.add(item!!)
                        break
                    } else if (item?.uid?.get(1).equals(userUid)) {
                        chatList.add(item!!)
                        break
                    } else {
                        continue
                    }
                }
                callback(chatList)
                tmp ++
            }
    }

}