package com.techascent.permissionmanager

import android.os.Parcel
import android.os.Parcelable


data class Options(

    var settingsText: String? = "Settings",
    var rationaleDialogTitle: String? = "Permissions Required",
    var settingsDialogTitle: String? = "Permissions Required",
    var settingsDialogMessage: String? = "Required permission(s) have been set"
        .plus(" not to ask again! Please provide them from settings."),
    var sendBlockedToSettings: Boolean = true,
    var createNewTask: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<Options> {
        override fun createFromParcel(parcel: Parcel): Options {
            return Options(parcel)
        }

        override fun newArray(size: Int): Array<Options?> {
            return arrayOfNulls(size)
        }
    }
}
