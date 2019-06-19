package com.example.hypechat.data.local

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "SpinKotlin"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    val AUTH_TOKEN = "auth_token"
    val TEAM_ID = "team_id"
    val USER_ID = "user_id"
    val USERNAME = "username"
    val PREF_COOKIES = "cookies"

    fun init(context: Context) {
        preferences = context.getSharedPreferences(
            NAME,
            MODE
        )
    }

    /**
     * SharedPreferences extension function, so we won't need to call edit() and apply()
     * ourselves on every SharedPreferences operation.
     */
    private inline fun SharedPreferences.edit(operation: (SharedPreferences.Editor) -> Unit) {
        val editor = edit()
        operation(editor)
        editor.apply()
    }

    fun clearSharedPreferences(){
        preferences.edit{
            it.clear()
        }
    }

    fun setToken(token: String) {
        preferences.edit {
            it.putString(AUTH_TOKEN, token)
        }
    }

    fun getToken(): String? {
        return preferences.getString(AUTH_TOKEN, null)
    }

    fun setUserId(userId: Int) {
        preferences.edit {
            it.putInt(USER_ID, userId)
        }
    }

    fun getUserId(): Int {
        return preferences.getInt(USER_ID, -1)
    }

    fun setUserName(username: String) {
        preferences.edit {
            it.putString(USERNAME, username)
        }
    }

    fun getUserName(): String? {
        return preferences.getString(USERNAME, null)
    }

    fun setTeamId(teamId: Int) {
        preferences.edit {
            it.putInt(TEAM_ID, teamId)
        }
    }

    fun getTeamId(): Int {
        return preferences.getInt(TEAM_ID, 0)
    }

    fun setCookies(cookies: HashSet<String>) {
        preferences.edit {
            it.putStringSet(PREF_COOKIES, cookies)
        }
    }

    fun getCookies(): HashSet<String>? {
        return preferences.getStringSet(PREF_COOKIES, null) as HashSet<String>?
    }

//    var language: String?
//        // custom getter to get a preference of a desired type, with a predefined default value
//        get() = preferences.getString(LANGUAGE.first, LANGUAGE.second)
//        // custom setter to save a preference back to preferences file
//        set(value) = preferences.edit {
//            it.putString(LANGUAGE.first, value)
//        }
}