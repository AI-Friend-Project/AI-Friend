package com.example.aifriend.recycler

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.ViewDetailActivity
import com.example.aifriend.data.MoreBoardData
import com.example.aifriend.databinding.ViewMoreboarddataBinding

class BoardAdapter (val context: Context, val itemList: List<MoreBoardData>): RecyclerView.Adapter<BoardAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ViewMoreboarddataBinding) : RecyclerView.ViewHolder(binding.root){
        var docId: String? = null
        fun bind (data: MoreBoardData){
            docId = data.docId
            binding.nameView.text = data.name
            binding.titleView.text = data.title
            binding.contentView.text = data.content
            binding.dateView.text = data.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) :MyViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewMoreboarddataBinding.inflate(layoutInflater))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: BoardAdapter.MyViewHolder, position: Int) {
        val data = itemList.get(position)
        holder.bind(itemList[position])

        //아이템클릭
        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView?.context, ViewDetailActivity::class.java)
            intent.putExtra("title", itemList[position].title)
            intent.putExtra("email", itemList[position].email)
            intent.putExtra("name", itemList[position].name)
            intent.putExtra("date", itemList[position].time)
            intent.putExtra("content", itemList[position].content)
            intent.putExtra("docId", itemList[position].docId)
            intent.putExtra("favName", itemList[position].favName)
            ContextCompat.startActivity(holder.itemView.context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}