package com.example.aifriend

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.FavData
import com.example.aifriend.databinding.FragmentTab2Binding
import com.example.aifriend.recycler.Tab2Adapter

class Tab2 : Fragment() {

    lateinit var binding: FragmentTab2Binding

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTab2Binding.inflate(inflater, container, false)
        setUpView()
        return binding.root
    }

    private fun setUpView(){
        val email = MyApplication.email.toString()
        makeRecyclerView(email)
    }

    private fun makeRecyclerView(email: String){
        MyApplication.db.collection("fav")
            //관심사 항목 안 users에 사용자 이메일이 추가되어 있는지 확인
            //user 에 fav 리스트 불러와도 될 거 같음
            .whereArrayContainsAny("users", listOf(email))
            .get()
            .addOnSuccessListener { result ->
                val itemList = mutableListOf<FavData>()
                for (document in result){
                    val item = document.toObject(FavData::class.java)
                    item.docId = document.id
                    itemList.add(item)
                }
                binding.tab2Recyclerview.layoutManager = LinearLayoutManager(activity)
                binding.tab2Recyclerview.adapter = Tab2Adapter(requireContext(), itemList)
            }.addOnFailureListener { exception ->
                Toast.makeText(activity, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}