package com.example.d_log

import android.Manifest
import android.app.Activity
import android.app.DirectAction
import android.content.ContentProviderClient
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import javax.xml.transform.SourceLocator

class DiaryActivity : AppCompatActivity() {

    /* CAMERA, STORAGE 권한 처리에 필요한 변수선언 */
    val CAMERA = arrayOf(Manifest.permission.CAMERA)
    val STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val CAMERA_CODE = 98
    val STORAGE_CODE = 99

    /* @ 사용자 위치를 받기 위한 변수선언 */
    lateinit var locationPermission: ActivityResultLauncher<Array<String>>
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback

    /* 사용자 위치(위도/경도), 주소, 이미지경로 저장 변수선언 */
    var tempLat:Double? = null
    var tempLon:Double? = null
    var tempAddr:String? = null
    var tempImagePath:String? = null
    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary)

        /* 카메라 button 클릭 시 카메라 촬영 함수 호출 */
        val camera = findViewById<ImageButton>(R.id.insert_cameraBtn)
        camera.setOnClickListener {
            callCamera()
        }

        /* @ 사용자 위치 받아오기 */
        // 권한 검사
        locationPermission = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions() ){ results->
            if(results.all { it.value }){
                Log.d("", "#21# 위치 권한 승인확인")
                /* GPS를 사용하여 위치 확인 및 실시간 위치 업데이트 함수 호출 */
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                updateLocation()
            } else{
                Toast.makeText(this, "위치에 대한 권한 승인이 필요합니다", Toast.LENGTH_LONG).show()
            }
        }
        // 권한 요청
        locationPermission.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

        /* save 버튼 클릭 시 data DB에 저장 */
        val saveBtn = findViewById<Button>(R.id.insert_saveBtn)
        val contentTxt = findViewById<EditText>(R.id.insert_EditContent)
        saveBtn.setOnClickListener {
            geoGetAddr()                                                                                        // i) 받은 실시간 사용자 위치(위도/경도)를 주소로 변환하여 tempAddr에 저장
            Log.d("DiaryActivity save버튼 클릭", "#21# save버튼 클릭 시 DB에 들어가는 값: ${tempImagePath.toString()}, ${tempAddr.toString()}, ${contentTxt.text}")
            val insertData = Diary(tempImagePath.toString(), tempAddr.toString(), contentTxt.text.toString())   // ii) DB에 추가해야 하는 Data 넣기

            // iii) DB에 추가(DBHelper 내 insert 함수
            val dbHelper = DBHelper.getInstance(this, "dlog.db")
            var result = dbHelper.insert(insertData)
            if (result == "SUCCESS"){
                Toast.makeText(this, "저장하였습니다 😉", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "저장에 실패하였습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }








    /* 권한요청 처리 결과 수신 함수 __ 권한 승인 검사 및 권한 요청 (자동 호출) */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_CODE -> {   // 카메라 권한
                for (grant in grantResults){
                    if (grant != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "카메라 권한의 승인이 필요합니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            STORAGE_CODE -> {  // 저장소 권한
                for (grant in grantResults){
                    if (grant != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "저장소 권한의 승인이 필요합니다.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /* (권고) 다른 권한도 확인이 가능하도록 하는 함수 */
    fun checkPermission(permissions: Array<out String>, type:Int) :Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for (permission in permissions){
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this, permissions, type)
                    return false
                }
            }
        }
        return true
    }

    /* 카메라 촬영 */
    fun callCamera(){
        if (checkPermission(CAMERA, CAMERA_CODE)){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, CAMERA_CODE)
        }
    }

    /* 사진 저장
    *  - Bitmap: 안드로이드에서 이미지를 표현하기 위해 사용되는 객체
    *  - Uri: 특정 리소스를 식별하는 통합 자원 식별자
    *  - MediaStore: 안드로이드 시스템에서 제공하는 미디어 데이터 DB */
    fun saveFile(fileName:String, mimeType:String, bitmap:Bitmap) :Uri? {

        var CV = ContentValues()

        CV.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)      // MediaStore에 '파일명, mimeType'을 지정
        CV.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){        // 안전성 검사
            CV.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CV)
        if (uri != null){
            var scriptor = contentResolver.openFileDescriptor(uri, "w")
            val fos = FileOutputStream(scriptor?.fileDescriptor)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                CV.clear()
                CV.put(MediaStore.Images.Media.IS_PENDING, 0)       // IS_PENDING 초기화
                contentResolver.update(uri, CV, null, null)
            }
        }
        return uri
    }

    /* 결과창 __ onActivityResult: Activity A와 B가 있을 때, A에서 B로 갔다가 다시 A로 넘어올 때 사용하는 Android에서 제공하는 기본 메소드 */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageView = findViewById<ImageView>(R.id.insert_imageView)
        val imagePathTxt = findViewById<TextView>(R.id.insert_imagePath)
        if (resultCode == Activity.RESULT_OK){
            when(requestCode){
                CAMERA_CODE -> {
                    if (data?.extras?.get("data") != null){
                        val img = data?.extras?.get("data") as Bitmap
                        val uri = saveFile(dateFileName(), "image/jpeg", img)


                        /* 저장한 사진의 실제 경로 확인 */
                        Log.d("DiaryActivity onActivityResult 함수", "#21# 이미지 경로: $uri")
                        Log.d("DiaryActivity onActivityResult 함수", "#21# 실제 이미지 경로: " + getPath(uri))

                        /* imageView내 이미지 표시 및 이미지 경로 출력 */
                        imageView.setImageURI(uri)
                        imagePathTxt.setText("[사진 경로] ${getPath(uri)}")
                        tempImagePath = getPath(uri)
                    }
                }
                STORAGE_CODE -> {
                    val uri = data?.data
                    imageView.setImageURI(uri)
                }
            }
        }
    }

    /* 파일명을 현재날짜로 지정하는 함수 */
    fun dateFileName() :String {
        val fileName = SimpleDateFormat("yyyyMMddHHmm").format(System.currentTimeMillis())
        return fileName
    }

    /* Uri가 들어오면 실제 경로(Path) String으로 변환  */
    fun getPath(uri: Uri?): String {
        val projection = arrayOf<String>(MediaStore.Images.Media.DATA)

        val cursor: Cursor = managedQuery(uri, projection, null, null, null)
        startManagingCursor(cursor)
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()

        return cursor.getString(columnIndex)
    }

    /* 사용자 위치정보 업데이트 */
    fun updateLocation(){
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 1000
        }

        // 요청결과 받기
        locationCallback = object :LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {    // 1초에 한번씩 변경된 위치 정보가 onLocationResult 로 전달된다
                locationResult?.let {
                    for (location in it.locations){
                        Log.d("DiaryActivity updateLocation 함수", "#21# 사용자 실시간 위치 → [위도]${location.latitude}  [경도]${location.longitude}")
                        tempLat = location.latitude
                        tempLon = location.longitude
                    }
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    /* 현재 사용자의 위치(위도/경도)를 받아 주소로 변환 */
    fun geoGetAddr(){
        var geocoder:Geocoder = Geocoder(this)

        var list:List<Address>? = null

        try {
            val lat:Double = tempLat!!
            val lon:Double = tempLon!!

            list = geocoder.getFromLocation(lat, lon, 10)
        } catch (e:IOException){
            Log.d("DiaryActivity geoGetAddr 함수", "#21# 위도/경도 → 주소 변환 시 입출력 error 발생")
        }

        if (list != null){
            if (list.isEmpty()){
                Toast.makeText(this, "해당되는 주소가 없습니다.", Toast.LENGTH_LONG).show()
            } else {
                Log.d("DiaryActivity geoGetAddr 함수", "#21# 위도/경도 → 주소 변환값: ${list[0].getAddressLine(0)}")
                tempAddr = list[0].getAddressLine(0).toString()
            }
        }
    }
}