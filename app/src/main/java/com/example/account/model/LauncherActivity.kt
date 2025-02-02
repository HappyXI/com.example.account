package com.example.account.model

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.account.util.LoginPreference

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetActivity = LoginActivity::class.java
        // 로그인 상태 확인
        /*val targetActivity = if (LoginPreference.isLoggedIn(this)) {
            MainActivity::class.java // 이미 로그인된 경우
        } else {
            LoginActivity::class.java // 로그인 필요
        }*/

        // 해당 액티비티로 이동
        startActivity(Intent(this, targetActivity))
        finish() // LauncherActivity 종료
    }
}