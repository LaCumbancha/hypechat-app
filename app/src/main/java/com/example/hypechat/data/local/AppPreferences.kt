package com.example.hypechat.data.local

import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val NAME = "SpinKotlin"
    private const val MODE = Context.MODE_PRIVATE
    private lateinit var preferences: SharedPreferences

    val AUTH_TOKEN = "auth_token"

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

    fun setToken(token: String) {
        preferences.edit {
            it.putString(AUTH_TOKEN, token)
        }
    }

    fun getToken(): String? {
        return preferences.getString(AUTH_TOKEN, null)
    }

//    var language: String?
//        // custom getter to get a preference of a desired type, with a predefined default value
//        get() = preferences.getString(LANGUAGE.first, LANGUAGE.second)
//        // custom setter to save a preference back to preferences file
//        set(value) = preferences.edit {
//            it.putString(LANGUAGE.first, value)
//        }
}