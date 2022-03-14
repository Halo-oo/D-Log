package com.example.d_log

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception

/* SQLiteOpenHelper 상속 */
class DBHelper(context: Context, DBFileName:String) :SQLiteOpenHelper(context, DBFileName, null, 1) {

    /* Singleton */
    companion object{
        private var dbHelper:DBHelper? = null       // instance값 (Class로부터 생성한 객체)

        fun getInstance(context: Context, DBFileName: String) :DBHelper {
            if (dbHelper == null){
                dbHelper = DBHelper(context, DBFileName)
            }
            return dbHelper!!
        }
    }

    /* TABLE 생성 */
    override fun onCreate(db: SQLiteDatabase?) {
        /* ADDRESS: 현재 위치 주소
        *  CURRENT_TIMESTAMP는 UTC(국제 표준시) / DATETIME('now', 'localtime')은 KST(한국 표준시) _UTC보다 9시간 빠르다 > KST 쿼리문이 ERROR나서 UTC로 했음 */
        var sql :String = "CREATE TABLE IF NOT EXISTS DLOG( " +
                            " SEQ INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            " IMAGEPATH STRING, " +
                            " ADDRESS STRING, " +
                            " CONTENT STRING, " +
                            " REGDATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL )"
        db?.execSQL(sql)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) { }


    /* RecyclerView, DetailView에 붙일 DB 값 꺼내오기 */
    fun allData() :MutableList<TempDlog> {
        Log.d("", "#21# DBHelper allData() 함수 실행")
        var sql = "SELECT * FROM DLOG"

        var db = this.writableDatabase
        var result = db.rawQuery(sql, null)

        // 문자열로 받은 후 *를 기준으로 자르기
        var dlogList :MutableList<TempDlog> = ArrayList<TempDlog>()
        var tempStr :String? = ""
        /* 주소는 RecyclerView에는 안붙일거지만 추후 DetailView로 이동시 사용해야 되서 같이 받기 */
        while (result.moveToNext()){
            tempStr = result.getString(result.getColumnIndex("REGDATE")) + "*" +
                        result.getString(result.getColumnIndex("IMAGEPATH")) + "*" +
                        result.getString(result.getColumnIndex("CONTENT")) + "*" +
                        result.getString(result.getColumnIndex("ADDRESS"))          // tempStr = 2022-02-18 HH:mm:ss*이미지경로*내용*주소
            Log.d("", "#21# DBHelper allData() DB로부터 가져온 값: ${tempStr}")
            var tempArr = tempStr.split("*")
            dlogList.add(TempDlog("${tempArr[0]}", "${tempArr[1]}", "${tempArr[2]}", "${tempArr[3]}"))
            Log.d("", "#21# DBHelper allData() DB로부터 가져온 값: ${dlogList}")
        }
        return dlogList
    }


    /* [C] insert */
    fun insert(vo:Diary) :String {
        var sql = "INSERT INTO DLOG(IMAGEPATH, ADDRESS, CONTENT) " +
                    "VALUES('${vo.imagepath}', '${vo.address}', '${vo.content}') "
        Log.d("DBHelper insert() 함수", "#21# DBHelper insert() 함수 SQL문: " + sql)

        var db = this.writableDatabase
        try {
            db.execSQL(sql)
            return "SUCCESS"
        } catch (e:Exception){
            e.printStackTrace()
            return "FAIL"
        }
    }
}