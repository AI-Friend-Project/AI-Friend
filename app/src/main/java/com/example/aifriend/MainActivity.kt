package com.example.aifriend

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aifriend.data.favData
import com.example.aifriend.databinding.ActivityMainBinding
import com.example.aifriend.databinding.FragmentTab1Binding
import com.example.aifriend.recycler.tab2Adapter
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout

//로그인 확인 후 맞는 화면 띄우기
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var binding1: FragmentTab1Binding

    lateinit var tab1: Tab1
    lateinit var tab2: Tab2
    lateinit var tab3: Tab3
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView

    //뒤로가기 종료
    var mBackWait:Long = 0

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

        tab1 = Tab1()
        tab2 = Tab2()
        tab3 = Tab3()

        //초기 탭 회면
        supportFragmentManager.beginTransaction().add(R.id.frameLayout, tab1).commit()
        //탭 선택시 프래그먼트 전환
        binding.mainTabMenu.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when(tab?.position){
                    0 -> {
                        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, tab1).commit()
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item!!.itemId){
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                makeRecyclerView()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun makeRecyclerView(){
        //친구목록띄우기
        MyApplication.db.collection("fav")
            .get()
            .addOnSuccessListener { result ->
                val itemList = mutableListOf<favData>()
                for (document in result){
                    val item = document.toObject(favData::class.java)
                    item.docId = document.id
                    itemList.add(item)
                }
                binding.navRecyclerView.layoutManager = LinearLayoutManager(this)
                binding.navRecyclerView.adapter = tab2Adapter(this, itemList)
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "서버로부터 데이터 획득에 실패했습니다.", Toast.LENGTH_SHORT).show()
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