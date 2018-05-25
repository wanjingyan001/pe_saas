package com.sogukj.pe.bean

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by admin on 2017/12/4.
 */
class WorkEducationBean() : Parcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(this.id)
        dest.writeString(this.employDate)
        dest.writeString(this.leaveDate)
        dest.writeString(this.company)
        dest.writeString(this.responsibility)
        dest.writeString(this.jobInfo)
        dest.writeString(this.department)
        dest.writeString(this.companyScale)
        dest.writeString(this.companyProperty)
        dest.writeInt(this.trade)
        dest.writeString(this.trade_name)
        dest.writeInt(this.pid)
        dest.writeByte(if (this.isShow) 1.toByte() else 0.toByte())
    }

    override fun describeContents(): Int = 0

    var id: Int = 0//
    var employDate: String? = null//入职时间
    var leaveDate: String? = null//离职时间
    var company: String? = null//公司
    var responsibility: String? = null//职能
    var jobInfo: String? = null//工作描述
    var department: String? = null//部门
    var companyScale: String? = null//公司规模
    var companyProperty: String? = null//公司性质
    var trade: Int = 0//行业id
    var trade_name: String? = null//行业名
    var pid: Int = 0//行业父id
    var isShow: Boolean = false//是否展示删除按钮

    constructor(parcel: Parcel) : this() {
        id = parcel.readInt()
        employDate = parcel.readString()
        leaveDate = parcel.readString()
        company = parcel.readString()
        responsibility = parcel.readString()
        jobInfo = parcel.readString()
        department = parcel.readString()
        companyScale = parcel.readString()
        companyProperty = parcel.readString()
        trade = parcel.readInt()
        trade_name = parcel.readString()
        pid = parcel.readInt()
        this.isShow = parcel.readByte().toInt() != 0
    }

    companion object CREATOR : Parcelable.Creator<WorkEducationBean> {
        override fun createFromParcel(parcel: Parcel): WorkEducationBean = WorkEducationBean(parcel)

        override fun newArray(size: Int): Array<WorkEducationBean?> = arrayOfNulls(size)
    }


}