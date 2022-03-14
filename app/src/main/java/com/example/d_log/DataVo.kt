package com.example.d_log

import android.os.Parcel
import android.os.Parcelable

class DataVo(val regdate: String?, val imagepath: String?, val content: String?) :Parcelable {

    /* parcel에 대한 기본생성자 */
    constructor(parcel: Parcel) :this(
        parcel.readString(), parcel.readString(), parcel.readString()
    ) { }

    /* describeContents(): Parcel하려는 object의 종류를 정의 */
    override fun describeContents(): Int {
        return 0
    }

    /* writeToParcel(): 실제 object를 serialization/flattening하는 메소드 */
    override fun writeToParcel(parcel: Parcel, p1: Int) {
        parcel.writeString(regdate)
        parcel.writeString(imagepath)
        parcel.writeString(content)
    }

    /* Parcelable.Creator: 객체를 수신하기 위해 CREATOR 작성 */
    companion object CREATOR :Parcelable.Creator<DataVo>{

        override fun createFromParcel(parcel: Parcel): DataVo {
            return DataVo(parcel)
        }

        override fun newArray(size: Int): Array<DataVo?> {
            return arrayOfNulls(size)
        }

    }


}