package com.example.hypechat.model

class User(val uid: String, val fullname: String, val profilePictureUrl: String){

    constructor() : this("", "", "")
}