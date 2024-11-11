package com.example.odomedapp.data

import com.example.odomedapp.data.User

object SessionManager {
    private var loggedInUser: User? = null

    fun saveUser(user: User) {
        loggedInUser = user
    }

    fun getUser(): User? {
        return loggedInUser
    }

    fun clearUser() {
        loggedInUser = null
    }

    fun isLoggedIn(): Boolean {
        return loggedInUser != null
    }
}