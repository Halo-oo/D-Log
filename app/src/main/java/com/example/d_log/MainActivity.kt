package com.example.d_log

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        /* RecyclerView 드로잉 */
        var recyclerView = findViewById<RecyclerView>(R.id.main_recyclerView)

        // i) DB 값 가져오기
        val dbHelder = DBHelper.getInstance(this, "dlog.db")
        var dlogList = dbHelder.allData()
        Log.d("main RecyclerView", "#21# main RecyclerView에 붙일려고 받아온 값: $dlogList")

        // ii) RecyclerView에 가져온 DB값 드로잉 __ DBHelper 내 recyclerData() 함수
        // >> 기능부터 구현하고 한줄에 2개씩 들어가도록 layout 수정하기, 폰트도 적용해보기
        val customAdapter = CustomAdapter(this, dlogList)
        recyclerView.adapter = customAdapter

        val layout = LinearLayoutManager(this)
        recyclerView.layoutManager = layout

        recyclerView.setHasFixedSize(true)


        /* 추가 Button 클릭 시 추가 페이지로 이동 */
        val insertBtn = findViewById<ImageButton>(R.id.main_addBtn)
        insertBtn.setOnClickListener {
            val nextIntent = Intent(this, DiaryActivity::class.java)
            startActivity(nextIntent)
        }
    }
}