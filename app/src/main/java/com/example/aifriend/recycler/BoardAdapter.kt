package com.example.aifriend.recycler

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.aifriend.BoardData
import com.example.aifriend.databinding.ItemBoardBinding


class BoardAdapter(val onItemClick: (BoardData) -> Unit) :
    RecyclerView.Adapter<BoardViewHolder>() {

    private val boardList = mutableListOf<BoardData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
        val binding =
            ItemBoardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d("tag","BoardAdapter")
        return BoardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
        holder.bind(boardList[position], onItemClick)
    }

    override fun getItemCount(): Int =
        boardList.size


    fun addAll(list: List<BoardData>) {
        boardList.clear()
        boardList.addAll(list.sortedBy { it.time })
        notifyDataSetChanged()
    }

    fun add(data: BoardData) {
        boardList.add(data)
        notifyItemChanged(boardList.lastIndex)
    }
}

class BoardViewHolder(private val binding: ItemBoardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(item: BoardData, onItemClick: (BoardData) -> Unit) {
        with(binding) {
            title.text = item.title
            content.text = item.content
            time.text = item.translateYear()
        }
        itemView.setOnClickListener { onItemClick(item) }
    }
}

