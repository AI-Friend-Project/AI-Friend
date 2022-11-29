package com.example.aifriend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.MoreBoardData
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ActivityFavdetailBinding
import com.example.aifriend.recycler.UserAdapter
import com.example.aifriend.recycler.BoardAdapter
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class FavdetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavdetailBinding
    //private var itemList = mutableListOf<userData>()
    private var itemList : MutableList<UserData> = ArrayList()
    private var userCount : Int = 0
    private var favName : String? = null

    //
    init{
        instance = this
    }
    companion object{
        private var instance:FavdetailActivity?=null
        fun getInstance():FavdetailActivity{
            return instance!!
        }
    }

    override fun onCreate (savedInstanceState: Bundle?){
        binding = ActivityFavdetailBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("favName").orEmpty()

        favName = intent.getStringExtra("favName")

        //인텐트 전달
        val intent = Intent(this, BoardViewActivity::class.java)
        intent.putExtra("favName", favName)
        val intent2 = Intent(this, FriendViewActivity::class.java)
        intent2.putExtra("favName", favName)

        //onResume 에서 작동함
        //getFavUsers()
        //makeMainBoardRecyclerView()

        //더보기 버튼 누르면 화면 전환
        binding.btnViewMore.setOnClickListener{
            startActivity(intent)
        }
        binding.btnFriendMore.setOnClickListener{
            startActivity(intent2)
        }
    }

    //필요없을수도
    fun getFavUsers(){
        //itemlist 리셋필요
        itemList = ArrayList()
        MyApplication.db.collection("fav").document(favName.toString())
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    var usersList = document.get("users") as List<String>
                    Log.d("tag", usersList.toString())
                    joinFriendList(favName.toString(), usersList)
                    //makeUsersRecyclerView(favName, usersList)
                    //binding.usersRecyclerView.layoutManager = LinearLayoutManager(this)
                    //binding.usersRecyclerView.adapter = favDetailAdapter(this, usersList)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    //현재 친구와 추가 요청된 친구 목록에서 제외하기
    //friend array가 저장되어있어야됨, 회원가입할 때 빈 값 추가하도록 수정함
    private fun joinFriendList(favName: String, usersList: List<String>){
        var joinList : MutableList<String> = ArrayList()
        var myFriend = listOf<String>()
        MyApplication.db.collection("user").document(MyApplication.email.toString())
            .get().addOnSuccessListener { document ->
                if(document.exists()){
                    myFriend = document.get("friends") as List<String>
                    Log.d("tag", "friends: ${myFriend.toString()}")
                    joinList.addAll(myFriend)
                }
            }.addOnFailureListener{ exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }

        var requestFriends = listOf<String>()
        MyApplication.db.collection("user").document(MyApplication.email.toString())
            .get().addOnSuccessListener { document ->
                if(document.exists()){
                    requestFriends = document.get("request") as List<String>
                    Log.d("tag", "request: ${requestFriends.toString()}")
                    joinList.addAll(requestFriends)
                    exceptMyFriend(usersList, joinList) //밖으로 빼야됨..why..?
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun exceptMyFriend(usersList: List<String>, friendsList: List<String>){
        Log.d("tag", "pass: ${friendsList}")
        var newList : MutableList<String> = ArrayList()
        newList.addAll(usersList)
        newList = (newList - MyApplication.email) as MutableList<String> //본인 제외
        newList = (newList - friendsList) as MutableList<String>
        Log.d("tag", "newList: ${newList}")

        makeUsersRecyclerView(newList)
    }

    private fun makeUsersRecyclerView(usersList: List<String>){
        Log.d("tag", "usersList: ${usersList}")
        userCount = usersList.size
        for (user in usersList) {
            MyApplication.db.collection("user")
                .whereEqualTo("email", user)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        var newList = mutableListOf<UserData>()
                        val item = document.toObject(UserData::class.java)
                        item.docId = document.id
                        newList.add(item)
                        saveItemList(newList)

                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
        //itemList 밖으로 전달이 안됨..
        Log.d("tag", "make: ${itemList.toString()}")
    }

    private fun saveItemList(newList : List<UserData>){
        itemList.addAll(newList)
        Log.d("tag", "saveitem: ${itemList}")
        Log.d("tag", "pre item count: ${itemList.size}, user count: ${userCount}")
        if(itemList.size == userCount){ //친구추천목록 안뜨면 여기 문제임
            Log.d("tag", "item count: ${itemList.size}, user count: ${userCount}")
            if(itemList.size > 3) {
                itemList = itemList.subList(0, 3)
            }
            //가로스크롤
            binding.mainUsersRecyclerView.layoutManager = LinearLayoutManager(this)
                .also { it.orientation = LinearLayoutManager.HORIZONTAL }
            binding.mainUsersRecyclerView.adapter = UserAdapter(this, itemList)
        }
    }

    //BoardViewActivity 와 동일
    //보이는 개수 제한
    private fun makeMainBoardRecyclerView(){
        MyApplication.db.collection("Board").document(favName.toString())
            .collection("post")
            .get()
            .addOnSuccessListener { result ->
                val itemList = mutableListOf<MoreBoardData>()
                for (document in result){
                    val item = document.toObject(MoreBoardData::class.java)
                    item.docId = document.id
                    itemList.add(item)
                }
                var itemSort = itemList.sortedByDescending { it.time }
                if (itemSort.size >4){
                    itemSort = itemSort.subList(0,4)
                }
                binding.mainBoardRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.mainBoardRecyclerView.adapter = BoardAdapter(this, itemSort)
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    //리사이클러뷰 업데이트
    override fun onResume() {
        super.onResume()
        makeMainBoardRecyclerView()
        getFavUsers()
    }

    //툴바 뒤로가기
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
