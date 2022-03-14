package com.example.d_log

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class CustomAdapter(private val context: Context, private val datalist: MutableList<TempDlog>) :RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.view_item_layout, parent, false)
        return ItemViewHolder(view)     // ItemViewHolder에 view_item_layout 반환
    }

    /* ItemViewHolder 내 bind 함수를 사용하여 받아온 dataList를 binding */
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(datalist[position], context)
    }

    override fun getItemCount(): Int {
        return datalist.size
    }

}


/* ItemViewHolder 클래스 */
class ItemViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    private val date = itemView.findViewById<TextView>(R.id.item_DateTxt)
    private val image = itemView.findViewById<ImageView>(R.id.item_imageView)
    private val content = itemView.findViewById<TextView>(R.id.item_ContentTxt)

    // bind() 함수
    fun bind(dLog: TempDlog, context: Context){
        /* case_1) 이미지 실제경로를 사용하여 이미지 붙이기 */
        Log.d("CustomAdatper", "#21# DB로부터 가지고온 Image실제경로: ${dLog.imagepath}")
        val file:File = File("${dLog.imagepath}")
        val fExist = file.exists()
        if (fExist){
            val diaryBitmap = BitmapFactory.decodeFile("${dLog.imagepath}")
            image.setImageBitmap(diaryBitmap)
        } else {
            Log.d("CustomAdatper", "#21# (Error)_DB로부터 가지고 온 Image실제경로의 Image가 없음")
        }

        /* case_2) 날짜, 내용 text 붙이기 */
        Log.d("CustomAdatper", "#21# DB로부터 가지고온 날짜, 내용: ${dLog.regdate}, ${dLog.content}")

        date.text = dLog.regdate
        content.text = dLog.content

        /* item 클릭 시 반응(detailView로 이동) __ Singleton 사용 */
        itemView.setOnClickListener {
            Intent(context, DetailActivity::class.java).apply {
                DetailSingleton.imagepath = dLog.imagepath
                DetailSingleton.address = dLog.address
                DetailSingleton.content = dLog.content
                DetailSingleton.regdate = dLog.regdate
            }.run { context.startActivity(this) }
        }

    }
}