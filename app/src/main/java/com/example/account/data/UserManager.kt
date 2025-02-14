package com.example.account.data

import android.content.Context

object UserManager {
    private const val PREFS_NAME = "UserPrefs"

    fun getUserName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("user_name", "이름 없음") ?: "이름 없음"
    }

    fun getUserId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("user_id", "ID 없음") ?: "ID 없음"
    }

    fun getJoinDate(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("user_join_date", "가입일 없음") ?: "가입일 없음"
    }
}
