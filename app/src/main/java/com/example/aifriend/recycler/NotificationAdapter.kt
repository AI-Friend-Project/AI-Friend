package com.example.aifriend.recycler

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.MyApplication
import com.example.aifriend.NotificationActivity
import com.example.aifriend.data.NotificationData
import com.example.aifriend.databinding.ViewNotidataBinding
import com.google.firebase.firestore.FieldValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar.getInstance

@RequiresApi(Build.VERSION_CODES.O)
class NotificationAdapter(val context: Context, val itemList: List<NotificationData>): RecyclerView.Adapter<NotificationAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ViewNotidataBinding) : RecyclerView.ViewHolder(binding.root){
        //
        private val notificationActivity = NotificationActivity.getInstance()

        var requestEmail : String? = null
        var docId: String? = null
        fun bind (data: NotificationData){
            requestEmail = data.requestEmail
            docId = data.docId
            binding.notiContent.text = data.content
            binding.notiTime.text = data.time

            //친구추가 알림일 때는 수락버튼 활성화
            if(data.type.toString() == "request"){
                binding.acceptBtn.visibility = View.VISIBLE
            }
        }
        init {
            binding.acceptBtn.setOnClickListener {
                MyApplication.db.collection("user").document(MyApplication.email.toString())
                    .update("friends", FieldValue.arrayUnion(requestEmail))
                MyApplication.db.collection("user").document(requestEmail.toString())
                    .update("friends", FieldValue.arrayUnion(MyApplication.email.toString()))
                //친구가 되었다는 메세지 추가
                var currentTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss")
                val nowTime = currentTime.format(formatter)
                var notification = mapOf(
                    "type" to "system",
                    "content" to "${requestEmail}님과 친구가 되었습니다.",
                    "time" to nowTime.toString()
                )
                MyApplication.db.collection("user").document(MyApplication.email.toString())
                    .collection("notification").add(notification)
                MyApplication.db.collection("user").document(requestEmail.toString())
                    .collection("notification").add(notification)
                //친구추가요청 메세지 삭제
                MyApplication.db.collection("user").document(MyApplication.email.toString())
                    .collection("notification").document(docId.toString()).delete()
                //binding.acceptBtn.visibility = View.GONE
                //request 목록에서 제거해야함, 배열에서 단일 항목 삭제는 안되고 전체 삭제하고 새로 배열을 만들어서 저장해야됨
                //업데이트 적용 잘 됨
                notificationActivity.makeNotiRecyclerView()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) :MyViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewNotidataBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: NotificationAdapter.MyViewHolder, position: Int) {
        val data = itemList.get(position)
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}