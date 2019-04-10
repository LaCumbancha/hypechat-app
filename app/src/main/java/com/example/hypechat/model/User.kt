package com.example.hypechat.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val fullname: String, val profilePictureUrl: String): Parcelable{

    constructor() : this("", "", "")
}