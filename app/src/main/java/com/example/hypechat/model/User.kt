package com.example.hypechat.model

class User(val uid: String, val fullname: String){

    var profilePictureUrl: String? = null

    constructor(uid: String, fullname: String, profilePictureUrl: String) : this(uid, fullname){
        this.profilePictureUrl = profilePictureUrl
    }
}