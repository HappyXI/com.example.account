package com.example.account.model

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.account.R
import com.example.account.util.DLoginPreference
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import android.util.Base64
import com.example.account.util.MemberFileHelper
import com.example.account.data.Member
import com.example.account.data.MemberManager
import org.json.JSONObject
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

class LoginActivity : AppCompatActivity() {

    private fun loginUser() { // 수정
        // 로그인 성공 처리 (공통 처리)
        DLoginPreference.setLoggedIn(this, true) // 로그인 상태 저장
        navigateToMain() // 메인 화면으로 이동
    }

    // 회원 정보 저장
    private fun saveUser(userId: String, name: String, email: String) {
        val member = Member(id = userId.hashCode(), name = name, email = email)
        MemberManager.addMember(member) // 앱 내 목록에 저장
        MemberFileHelper.saveMembers(this, MemberManager.getMembers()) // CSV 파일 저장
    }

    // 🛠 Google ID Token을 JSON 데이터로 변환하는 함수
    private fun parseGoogleIdToken(idToken: String): JSONObject? {
        return try {
            val parts = idToken.split(".")
            if (parts.size != 3) {
                null
            } else {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE), Charsets.UTF_8)
                JSONObject(payload)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Google 로그인 처리
    private suspend fun startGoogleLogin() {
        val credentialManager = CredentialManager.create(this@LoginActivity)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("592979088397-uuumos1fbqposrgavp9ch67jddtr39i0.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            // 🛠 Credential Manager에서 Google 로그인 정보 가져오기
            val result = credentialManager.getCredential(this@LoginActivity, request)

            // ✅ 최신 방식: GoogleIdTokenCredential에서 ID Token 가져오기
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = googleIdTokenCredential.idToken

            if (googleIdToken == null) {
                Log.e("GoogleLogin", "Google ID Token이 없음")
                Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
                return
            }

            Log.i("GoogleLogin", "Token: $googleIdToken")

            // 🛠 Google ID Token을 JSON으로 변환하여 사용자 정보 가져오기
            val json = parseGoogleIdToken(googleIdToken)

            val googleId = json?.optString("sub", "Unknown") ?: "Unknown"
            val name = json?.optString("name", "Unknown") ?: "Unknown"
            val email = json?.optString("email", "No Email") ?: "No Email"

            Log.i("GoogleLogin", "사용자 ID: $googleId")
            Log.i("GoogleLogin", "이름: $name")
            Log.i("GoogleLogin", "이메일: $email")

            saveUser(googleId, name, email)

            Toast.makeText(this, "Google 로그인 성공!", Toast.LENGTH_SHORT).show()
            navigateToMain()

        } catch (e: Exception) {
            Log.e("GoogleLogin", "Google 로그인 실패: ${e.message}", e)
            Toast.makeText(this, "Google 로그인 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 네이버 로그인 처리
    private fun startNaverLogin() {
        NaverIdLoginSDK.authenticate(this, object : OAuthLoginCallback {
            override fun onSuccess() {
                Log.d("LoginActivity", "Naver Login Success")
                fetchNaverUserInfo() // 사용자 정보 요청
                navigateToMain()
                loginUser()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e("LoginActivity", "Naver Login Failed: $httpStatus, $message")
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDesc = NaverIdLoginSDK.getLastErrorDescription()
                Log.e("NaverLoginError", "Error Code: $errorCode, Description: $errorDesc")
            }

            override fun onError(errorCode: Int, message: String) {
                Log.e("LoginActivity", "Naver Login Error: $errorCode, $message")
            }
        })
    }

    private fun fetchNaverUserInfo() {
        val oauthLogin = NidOAuthLogin()
        oauthLogin.callProfileApi(object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(result: NidProfileResponse) {
                val profile = result.profile

                val naverId = profile?.id ?: return
                val name = profile?.name ?: "Unknown"
                val email = profile?.email ?: "No Email"

                Log.d("LoginActivity", "Naver User ID: $naverId")
                Log.d("LoginActivity", "Naver User Name: $name")
                Log.d("LoginActivity", "Naver User Email: $email")

                saveUser(naverId, name, email)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e("LoginActivity", "Failed to fetch Naver user info: $httpStatus, $message")
            }

            override fun onError(errorCode: Int, message: String) {
                Log.e("LoginActivity", "Error fetching Naver user info: $errorCode, $message")
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 네이버 로그인 SDK 초기화
        NaverIdLoginSDK.initialize(
            this,
            getString(R.string.naver_client_id),
            getString(R.string.naver_client_secret),
            getString(R.string.naver_client_name)

        )

        // Kakao SDK 초기화
        KakaoSdk.init(
            this,
            getString(R.string.kakao_app_key)
        )

        // Google 로그인 버튼 클릭 리스너
        val btnGoogleLogin = findViewById<Button>(R.id.btn_google_login)
        btnGoogleLogin.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                startGoogleLogin() // Google 로그인 처리 함수
            }
        }

        // 네이버 로그인 버튼 클릭 리스너
        val btnNaverLogin = findViewById<Button>(R.id.btn_naver_login)
        btnNaverLogin.setOnClickListener {
            startNaverLogin()
        }

        // 카카오 로그인 버튼 클릭 리스너
        val kakaoLoginButton = findViewById<Button>(R.id.btn_kakao_login)
        kakaoLoginButton.setOnClickListener {
            loginWithKakao()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // 현재 LoginActivity 종료
    }

    // 카카오 로그인 처리
    private fun loginWithKakao() {
        // 카카오 계정으로 로그인
        UserApiClient.instance.loginWithKakaoAccount(this) { token, error ->
            if (error != null) {
                Log.e("KakaoLogin", "로그인 실패", error)
            } else if (token != null) {
                Log.i("KakaoLogin", "로그인 성공: ${token.accessToken}")
                fetchKakaoUserInfo()
                navigateToMain()
                loginUser() // 수정
            }
        }
    }

    private fun fetchKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoLogin", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                val kakaoId = user.kakaoAccount?.email ?: "No kakaoId"
                val name = user.kakaoAccount?.profile?.nickname ?: "Unknown"
                val email = user.kakaoAccount?.email ?: "No Email"

                Log.i("KakaoLogin", "사용자 정보 요청 성공")
                Log.i("KakaoLogin", "닉네임: ${user.kakaoAccount?.profile?.nickname}")
                Log.i("KakaoLogin", "이메일: ${user.kakaoAccount?.email}")

                saveUser(kakaoId, name, email)
            }
        }
    }
}