package com.example.aifriend.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.MoreBoardData
import com.example.aifriend.databinding.ViewMoreboarddataBinding

class MoreBoardAdapter (val context: Context, val itemList: List<MoreBoardData>): RecyclerView.Adapter<MoreBoardAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ViewMoreboarddataBinding) : RecyclerView.ViewHolder(binding.root){
        var docId: String? = null
        fun bind (data: MoreBoardData){
            docId = data.docId
            binding.emailView.text = data.email
            binding.titleView.text = data.title
            binding.contentView.text = data.content
            binding.dateView.text = data.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) :MyViewHolder{
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewMoreboarddataBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: MoreBoardAdapter.MyViewHolder, position: Int) {
        val data = itemList.get(position)
        holder.bind(itemList[position])

        //아이템클릭
        holder.itemView.setOnClickListener{}
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}