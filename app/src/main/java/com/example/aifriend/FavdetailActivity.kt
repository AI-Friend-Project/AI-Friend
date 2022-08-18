package com.example.aifriend

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.favData
import com.example.aifriend.data.userData
import com.example.aifriend.databinding.ActivityFavdetailBinding
import com.example.aifriend.recycler.BoardAdapter
import com.example.aifriend.recycler.favDetailAdapter
import com.example.aifriend.recycler.tab2Adapter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@RequiresApi(Build.VERSION_CODES.O)
class FavdetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavdetailBinding
    //private var itemList = mutableListOf<userData>()
    private var itemList : MutableList<userData> = ArrayList()
    private var userCount : Int = 0

    //게시물
    private val boardAdapter = BoardAdapter { clickBoardItem ->
    }
    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == 2000) {
                intent.getStringExtra("favName")?.let {
                    getBoardData(it)
                }
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

        var favName = intent.getStringExtra("favName")
        binding.favNameView.text = favName

        getFavUsers(favName.toString())

        //게시물
        binding.rvBoard.adapter = boardAdapter

        //툴바로 위치 옮겨야됨
        binding.fab.setOnClickListener {
            startForResult.launch(
                Intent(this, AddBoardActivity::class.java).apply {
                    putExtra("favorite", intent.getStringExtra("favName"))
                }
            )
        }

        intent.getStringExtra("favName")?.let {
            getBoardData(it)
            Log.d("tag", "intent: ${it}")
        }
    }

    //필요없을수도
    private fun getFavUsers(favName: String){
        MyApplication.db.collection("fav").document(favName)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    var usersList = document.get("users") as List<String>
                    Log.d("tag", usersList.toString())
                    joinFriendList(favName, usersList)
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
        userCount = usersList.count()
        for (user in usersList) {
            MyApplication.db.collection("user")
                .whereEqualTo("email", user)
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        var newList = mutableListOf<userData>()
                        val item = document.toObject(userData::class.java)
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

    private fun saveItemList(newList : List<userData>){
        itemList.addAll(newList)
        Log.d("tag", "saveitem: ${itemList}")
        Log.d("tag", "pre item count: ${itemList.count()}, user count: ${userCount}")
        if(itemList.count() == userCount){ //친구추천목록 안뜨면 여기 문제임
            Log.d("tag", "item count: ${itemList.count()}, user count: ${userCount}")
            //가로스크롤
            binding.usersRecyclerView.layoutManager = LinearLayoutManager(this)
                .also { it.orientation = LinearLayoutManager.HORIZONTAL }
            binding.usersRecyclerView.adapter = favDetailAdapter(this, itemList)
        }
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

    //게시물 부분
    private fun getBoardData(favorite: String) {
        MyApplication.db.collection("Board").document(favorite).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.exists()) {
                    val getResult: java.util.ArrayList<HashMap<String, String>>? =
                        it.result.get("list") as java.util.ArrayList<HashMap<String, String>>?
                    val toResultList = getResult?.map { it.toBoardData() }
                    if (!toResultList.isNullOrEmpty()) {
                        boardAdapter.addAll(toResultList)
                    }
                } else {
                    createBoardCollect(favorite)
                }
            }
        }
    }

    private fun createBoardCollect(favorite: String) {
        MyApplication.db.collection("Board").document(favorite).set(emptyMap<String, BoardData>())
    }
}

//따로 파일로 빼기
data class BoardData(
    val title: String,
    val content: String,
    val name: String,
    val time: String = SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().time)
) {
    fun translateYear(): String {
        return "${time.substring(0, 4)}/${time.substring(4, 6)}/${time.substring(6, 8)}"
    }
}


fun HashMap<String, String>.toBoardData(): BoardData =
    BoardData(
        title = getValue("title"),
        content = getValue("content"),
        name = getValue("name"),
        time = getValue("time")
    )