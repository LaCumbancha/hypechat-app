package com.example.hypechat.data.model

class ChatMessage(val id: String, val fromId: String, val toId: String, val message: String, val timestamp: Long) {

    constructor() : this("", "", "", "", -1)
}