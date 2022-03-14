package com.example.d_log

import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.File
import java.io.IOException

class DetailActivity : AppCompatActivity()/*, OnMapReadyCallback*/ {

    private lateinit var mMap: GoogleMap
    var latitude:Double? = null
    var longitude:Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        /* 뒤로가기 버튼 활성화 */
        val previous = findViewById<ImageButton>(R.id.detail_previousBtn)
        previous.setOnClickListener {
            val nextIntent = Intent(this, MainActivity::class.java)
            startActivity(nextIntent)
        }

        /* DeitaView 드로잉 */
        val date = findViewById<TextView>(R.id.detail_DateTxt)
        val image = findViewById<ImageView>(R.id.detail_Imageview)
        val content = findViewById<TextView>(R.id.detail_ContentTxt)

        /* i) image 드로잉 */
        val file: File = File("${DetailSingleton.imagepath}")
        val fExist = file.exists()
        if (fExist){
            val diaryBitmap = BitmapFactory.decodeFile("${DetailSingleton.imagepath}")
            image.setImageBitmap(diaryBitmap)
        } else {
            Log.d("DetailActivity", "#21# (Error)_DetailActivity Image실제경로의 Image가 없음")
        }

        /* ii) text 드로잉 */
        date.text = DetailSingleton.regdate
        content.text = DetailSingleton.content


        /* locaton 버튼 클릭 시 Google Map 띄우기 */
        val locationBtn = findViewById<ImageButton>(R.id.detail_locationBtn)
        var geocoder:Geocoder = Geocoder(this)

        locationBtn.setOnClickListener {
            var list:List<Address>? = null
            var str = DetailSingleton.address.toString()
            Log.d("DetailActivity", "#21# 가지고온 주소값 확인: ${str}")

            try {
                list = geocoder.getFromLocationName(str, 10)
            } catch (e:IOException){
                Log.d("DetailActivity", "#21# DetailView 주소 입출력 오류 발생")
            }

            if (list != null){
                if (list!!.isEmpty()){
                    Toast.makeText(this, "해당되는 주소를 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                } else {
                    val addr = list!![0]
                    /*val lat = addr.latitude
                    val lon = addr.longitude*/
                    latitude = addr.latitude
                    longitude = addr.longitude
                    val geo = String.format("geo:%f, %f", latitude, longitude)
                    Log.d("", "#21# 주소 → 위도/경도 변환 값: $geo")

                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(geo))
                    startActivity(intent)
                }
            }
        }

    }

    // 구글 맵 Fragment 했던거 참고해서 다시 해보기 > 할려는거: google map에 marker 찍기
    /* 구글 맵 준비 *//*
    fun startProcess(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("", "#21# onMapReady 실행확인")
        var mMap = googleMap

        setLocation(latitude!!.toDouble(), longitude!!.toDouble())
    }


    fun setLocation(lat:Double, lon:Double){
        val LATLNG = LatLng(lat, lon)
        val makerOptions = MarkerOptions().position(LATLNG).title("📍 Here!")
        val cameraPosition = CameraPosition.Builder().target(LATLNG).zoom(15.0f).build()

        mMap.clear()
        mMap.addMarker(makerOptions)
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }*/
}