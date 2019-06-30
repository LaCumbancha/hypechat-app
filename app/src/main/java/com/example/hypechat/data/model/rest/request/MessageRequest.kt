package com.example.hypechat.data.model.rest.request

class MessageRequest (val chat_id: Int, val team_id: Int, val content: String, val message_type: String, val mentions: List<Int>)