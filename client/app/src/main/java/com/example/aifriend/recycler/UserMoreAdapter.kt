package com.example.aifriend.recycler

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.FavdetailActivity
import com.example.aifriend.FriendViewActivity
import com.example.aifriend.MyApplication
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ViewUserdata2Binding
import com.google.firebase.firestore.FieldValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class UserMoreAdapter(val context: Context, val usersList: List<UserData>): RecyclerView.Adapter<UserMoreAdapter.MyViewHolder>(){

    inner class MyViewHolder(val binding: ViewUserdata2Binding) : RecyclerView.ViewHolder(binding.root){

        //
        private var friendViewActivity = FriendViewActivity.getInstance()

        var name : String? = null
        var email : String? = null
        fun bind(data: UserData){
            name = data.name
            email = data.email
            binding.NameView.text = data.name
        }
        init {
            binding.requestBtn.setOnClickListener {
                request(email.toString())
                //수락하기 버튼 누르면 수락 버튼 사라짐->필요없어짐
                //binding.requestBtn.visibility = View.INVISIBLE
                //수락하기 누르면 친구 추천 리스트 업데이트
                friendViewActivity.getFavUsers2()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewUserdata2Binding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = usersList.get(position)
        holder.bind(usersList[position])

        //아이템클릭
        holder.itemView.setOnClickListener{
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    private fun request(email: String){
        MyApplication.db.collection("user").document(MyApplication.email.toString())
            .update("request", FieldValue.arrayUnion(email))
        Toast.makeText(context, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
        Log.d("tag", email.toString())
        //친구 요청 알림 (알림 - 친구 요청, 시스템 알림)
        var currentTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
        val nowTime = currentTime.format(formatter)
        var message = "${MyApplication.email}님이 친구 요청을 보냈습니다."
        val notification = mapOf(
            "type" to "request",
            "content" to message,
            "time" to nowTime.toString(),
            "requestEmail" to MyApplication.email
        )
        MyApplication.db.collection("user").document(email.toString())
            .collection("notification").add(notification)
        //요청 친구 목록에 추가
        MyApplication.db.collection("user").document(MyApplication.email.toString())
            .update("request", FieldValue.arrayUnion(email))
        MyApplication.db.collection("user").document(email.toString())
            .update("request", FieldValue.arrayUnion(MyApplication.email.toString()))
        //notifyDataSetChanged()
    }
}