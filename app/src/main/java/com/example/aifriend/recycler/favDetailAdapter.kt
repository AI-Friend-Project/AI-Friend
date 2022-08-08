package com.example.aifriend.recycler

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.FavdetailActivity
import com.example.aifriend.MyApplication
import com.example.aifriend.MyApplication.Companion.email
import com.example.aifriend.data.userData
import com.example.aifriend.databinding.ViewUserdataBinding
import com.google.firebase.firestore.FieldValue
import java.lang.reflect.Member
import java.text.Collator.getInstance
import java.text.DateFormat.getInstance
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar.getInstance

@RequiresApi(Build.VERSION_CODES.O)
class favDetailAdapter(val context: Context, val usersList: List<userData>): RecyclerView.Adapter<favDetailAdapter.MyViewHolder>(){

    inner class MyViewHolder(val binding: ViewUserdataBinding) : RecyclerView.ViewHolder(binding.root){

        var name : String? = null
        var email : String? = null
        fun bind(data: userData){
            name = data.name
            email = data.email
            binding.NameView.text = data.name
            binding.emailView.text = data.email
        }
        init {
            binding.requestBtn.setOnClickListener {
                request(email.toString())
                //수락하기 버튼 누르면 수락 버튼 사라짐
                binding.requestBtn.visibility = View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewUserdataBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = usersList.get(position)
        holder.bind(usersList[position])

        //아이템클릭
        holder.itemView.setOnClickListener{}
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    private fun request(email: String){
        MyApplication.db.collection("user").document(MyApplication.email.toString())
        .update("request",FieldValue.arrayUnion(email))
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
        .update("request",FieldValue.arrayUnion(email))
        MyApplication.db.collection("user").document(email.toString())
        .update("request",FieldValue.arrayUnion(MyApplication.email.toString()))
        //notifyDataSetChanged()
    }
}

