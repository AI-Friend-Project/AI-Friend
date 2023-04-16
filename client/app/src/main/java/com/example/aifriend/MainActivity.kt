package com.example.aifriend

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.UserData
import com.example.aifriend.databinding.ActivityMainBinding
import com.example.aifriend.databinding.FragmentTab1Binding
import com.example.aifriend.databinding.HeaderBinding
import com.example.aifriend.loading.LoadingActivity
import com.example.aifriend.recycler.NavAdapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout

//로그인 확인 후 맞는 화면 띄우기
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var binding1: FragmentTab1Binding
    private lateinit var binding2: HeaderBinding

    lateinit var tab1: Tab1
    lateinit var tab2: Tab2
    lateinit var tab3: Tab3
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView

    lateinit var chat1: ChatFragment

    //뒤로가기 종료
    var mBackWait:Long = 0

    private var itemList : MutableList<UserData> = ArrayList()
    private var friendsCount : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    override fun onStart() {
        super.onStart()
        if(!MyApplication.checkAuth()){ //로그인 안 되어 있다면 로그인 화면으로
            startActivity(Intent(this, LoginActivity::class.java))
        }else { //로그인 되어 있다면 메인 화면으로 이동
            main()
        }
    }

    private fun main(){
        binding = ActivityMainBinding.inflate(layoutInflater)
        binding1 = FragmentTab1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //툴바
        val toolbar = binding.mainToolbar as androidx.appcompat.widget.Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_24)

        //네비게이션 드로어
        drawerLayout = binding.drawerLayout
        navigationView = binding.navView
        val headerView = navigationView.getHeaderView(0)

        tab1 = Tab1()
        tab2 = Tab2()
        tab3 = Tab3()

        chat1 = ChatFragment()

        //초기 탭 회면
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, chat1).commit()
        //탭 선택시 프래그먼트 전환
        binding.mainTabMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, chat1).commit()
                    }
                    1 -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, tab2).commit()
                    }
                    2 -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, tab3).commit()
                    }
                }
            }
        })
    }

    //툴바 메뉴 연결
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    //툴바 선택
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home -> {
                friendsCount = 0
                itemList.clear()
                drawerLayout.openDrawer(GravityCompat.START)
                getInformation()
                getFriendsList()
            }
            R.id.menu_notification -> {
                startActivity(Intent(this, NotificationActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getInformation(){ //헤더에 내 정보 띄우기
        val header: View = navigationView.getHeaderView(0)
        val userEmail: TextView = header.findViewById(R.id.headerEmailView)
        val userName: TextView = header.findViewById(R.id.headerNameView)
        userEmail.text = MyApplication.email
        val e = MyApplication.email.toString()
        val temp = e.split("@")
        var name = temp[0]
        userName.text = name
    }

    private fun getFriendsList(){
        //친구목록띄우기
        var myFriend = listOf<String>()
        MyApplication.db.collection("user").document(MyApplication.email.toString())
            .get().addOnSuccessListener { document ->
                if(document.exists()){
                    myFriend = document.get("friends") as List<String>
                    myFriend = myFriend - ""
                    Log.d("tag", "get friends: ${myFriend.toString()}")
                    makeRecyclerView(myFriend)
                }
            }.addOnFailureListener{ exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }
    private fun makeRecyclerView(friendsList: List<String>){
        friendsCount = friendsList.count()
        for (user in friendsList) {
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
    }

    private fun saveItemList(newList : List<UserData>){
        itemList.addAll(newList)
        Log.d("tag", "saveitem: ${itemList}")
        if(itemList.count() == friendsCount){
            Log.d("tag", "item count: ${itemList.count()}, user count: ${friendsCount}")
            binding.navRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.navRecyclerView.adapter = NavAdapter(this, itemList)
            //itemList.clear()
        }
    }

    override fun onBackPressed() {
        // 뒤로가기 버튼 클릭
        if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
            Toast.makeText(this, "뒤로가기를 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            finish() //액티비티 종료
        }
    }
}