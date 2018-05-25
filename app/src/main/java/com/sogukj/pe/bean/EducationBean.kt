package com.sogukj.pe.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by admin on 2017/12/4.
 * 简历-添加教育经历对象
 */
class EducationBean() : Parcelable {
    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeValue(id)
        dest?.writeString(toSchoolDate)
        dest?.writeString(graduationDate)
        dest?.writeString(school)
        dest?.writeString(education)
        dest?.writeString(major)
        dest?.writeString(majorInfo)
        dest?.writeInt(if(isShow) 0 else 1)
    }

    override fun describeContents(): Int = 0

    var id: Int? = null
    var toSchoolDate: String = ""//入学时间（时间格式如  2017/10）
    var graduationDate: String = ""//毕业时间（时间格式如  2017/10）
    var school: String = ""//学校名称
    var education: String = ""//学历
    var major: String = ""//专业
    var majorInfo: String? = null//专业描述
    var isShow: Boolean = false//是否展示删除按钮

    constructor(parcel: Parcel) : this() {
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        toSchoolDate = parcel.readString()
        graduationDate = parcel.readString()
        school = parcel.readString()
        education = parcel.readString()
        major = parcel.readString()
        majorInfo = parcel.readString()
        isShow = parcel.readInt()!= 1
    }

    companion object CREATOR : Parcelable.Creator<EducationBean> {
        override fun createFromParcel(parcel: Parcel): EducationBean = EducationBean(parcel)

        override fun newArray(size: Int): Array<EducationBean?> = arrayOfNulls(size)
    }
}