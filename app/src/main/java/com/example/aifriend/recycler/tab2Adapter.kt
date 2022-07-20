package com.example.aifriend.recycler

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.FavdetailActivity
import com.example.aifriend.data.favData
import com.example.aifriend.databinding.ViewFavdataBinding

class tab2Adapter (val context: Context, val itemList: List<favData>): RecyclerView.Adapter<tab2Adapter.MyViewHolder>(){
    inner class MyViewHolder(val binding: ViewFavdataBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind (data: favData){
            binding.favNameView.text=data.favName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyViewHolder(ViewFavdataBinding.inflate(layoutInflater))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = itemList.get(position)
        holder.bind(itemList[position])
        holder.itemView.setOnClickListener{
            var intent = Intent(holder.itemView?.context, FavdetailActivity::class.java)
            intent.putExtra("favName", itemList[position].favName)
            ContextCompat.startActivity(holder.itemView.context,intent,null)
            Toast.makeText(context, "click", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }


}