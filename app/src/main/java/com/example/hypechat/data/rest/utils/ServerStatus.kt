package com.example.hypechat.data.rest.utils

enum class ServerStatus(val status: String) {
    WRONG_CREDENTIALS("WRONG_CREDENTIALS"),
    OK("OK"),
    ACTIVE("ACTIVE"),
    ALREADY_REGISTERED("ALREADY_REGISTERED"),
    LOGGED_OUT("LOGGED_OUT"),
    WRONG_TOKEN("WRONG_TOKEN"),
    SENT("SENT"),
    USER_NOT_FOUND("USER_NOT_FOUND"),
    CHAT_NOT_FOUND("CHAT_NOT_FOUND"),
    CREATED("CREATED"),
    ERROR("ERROR"),
    ADDED("ADDED"),
    NOT_ENOUGH_PERMISSIONS("NOT_ENOUGH_PERMISSIONS"),
    TEAM_NOT_FOUND("TEAM_NOT_FOUND"),
    LIST("LIST"),
    ROLE_MODIFIED("ROLE_MODIFIED"),
    UPDATED("UPDATED"),
    REMOVED("REMOVED"),
    INVITED("INVITED"),
    ALREADY_INVITED("ALREADY_INVITED"),
    JOINED("JOINED")
}