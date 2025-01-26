/*plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services") // Firebase 플러그인
}

android {
    namespace = "com.example.account"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.account"
        minSdk = 26
        //noinspection EditedTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.database.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 기존 Firebase BOM 설정
    implementation(platform("com.google.firebase:firebase-bom:33.8.0"))
    // Firebase Authentication 추가
    implementation("com.google.firebase:firebase-auth")

    // Google Play Services Auth 추가 (Google 로그인)
    implementation("com.google.android.gms:play-services-auth:31.3.0")

    // 네이버 로그인
    implementation("com.navercorp.nid:oauth-jdk8:5.10.0")

    // 카카오 로그인
    implementation("com.kakao.sdk:v2-user:2.20.6") // 카카오 로그인 API 모듈
    implementation("com.kakao.sdk:v2-share:2.20.6") // 카카오톡 공유 API 모듈

    // PostgreSQL JDBC Driver 추가
    implementation("org.postgresql:postgresql:42.7.5")

}*/