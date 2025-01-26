package com.example.account.model

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.account.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.user.UserApiClient

class LoginActivity : AppCompatActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Google Sign-In Client 초기화
        googleSignInClient = GoogleSignIn.getClient(
            this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build())

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
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
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

    // 네이버 로그인 처리
    private fun startNaverLogin() {
        val oauthLogin = NidOAuthLogin()
        oauthLogin.accessToken(this, object : OAuthLoginCallback {
            override fun onSuccess() {
                Log.d("LoginActivity", "Naver Login Success")
                fetchNaverUserInfo()
                navigateToMain()
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
            override fun onSuccess(response: NidProfileResponse) {
                val profile = response.profile
                Log.d("LoginActivity", "Naver User ID: ${profile?.id}")
                Log.d("LoginActivity", "Naver User Name: ${profile?.name}")
                Log.d("LoginActivity", "Naver User Email: ${profile?.email}")
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.e("LoginActivity", "Failed to fetch Naver user info: $httpStatus, $message")
            }

            override fun onError(errorCode: Int, message: String) {
                Log.e("LoginActivity", "Error fetching Naver user info: $errorCode, $message")
            }
        })
    }

    // Google 로그인 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                if (task == null || !task.isSuccessful) {
                    Log.e("LoginActivity", "Google Sign-In failed: Task is null or unsuccessful")
                    throw ApiException(Status.RESULT_CANCELED)
                }
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken
                if (idToken.isNullOrEmpty()) {
                    Log.e("LoginActivity", "Google ID Token is null or empty")
                    return
                }
                firebaseAuthWithGoogle(idToken)
            } catch (e: ApiException) {
                Log.e("LoginActivity", "Google Sign-In failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // val user = FirebaseAuth.getInstance().currentUser
                    navigateToMain()
                    // 성공 시 다음 화면으로 이동
                } else {
                    // 실패 처리
                }
            }
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
            }
        }
    }

    private fun fetchKakaoUserInfo() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("KakaoLogin", "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i("KakaoLogin", "사용자 정보 요청 성공")
                Log.i("KakaoLogin", "닉네임: ${user.kakaoAccount?.profile?.nickname}")
                Log.i("KakaoLogin", "이메일: ${user.kakaoAccount?.email}")
            }
        }
    }
}
