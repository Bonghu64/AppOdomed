package com.example.odomedapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

object SessionManager {
    private const val PREF_NAME = "UserSession"
    private const val USER_KEY = "loggedInUser"

    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()

    // Inicializa el SharedPreferences (llamar en Application o Activity)
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit().putString(USER_KEY, userJson).apply()
    }

    fun getUser(): User? {
        val userJson = sharedPreferences.getString(USER_KEY, null)
        return if (userJson != null) gson.fromJson(userJson, User::class.java) else null
    }

    fun clearUser() {
        sharedPreferences.edit().remove(USER_KEY).apply()
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.contains(USER_KEY)
    }
}
