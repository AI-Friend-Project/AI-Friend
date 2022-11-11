package com.example.aifriend.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ViewNavdataBinding

class NavAdapter(val context: Context, val itemList: List<UserData>): RecyclerView.Adapter<NavAdapter.MyViewHolder>() {
    inner class MyViewHolder(val binding: ViewNavdataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data: UserData){
            binding.NameView.text = data.name
            binding.emailView.text = data.email
        }
        init {
            binding.textBtn.setOnClickListener {  } //채팅시작
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavAdapter.MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewNavdataBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: NavAdapter.MyViewHolder, position: Int) {
        val data = itemList.get(position)
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}