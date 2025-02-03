import com.android.build.api.dsl.Packaging

plugins {
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

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.runtime.tracing)
    implementation(libs.androidx.ui)
    implementation(libs.firebase.database.ktx)
    implementation(libs.googleid)
    implementation(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // 기존 Firebase BOM 설정
    implementation(platform(libs.firebase.bom))
    // Firebase Authentication 추가
    implementation(libs.firebase.auth)

    // Google Play Services Auth 추가 (Google 로그인)
    implementation(libs.androidx.credentials)

    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.play.services.auth)

    // 네이버 로그인
    implementation(libs.oauth.jdk8)

    // 카카오 로그인
    implementation(libs.v2.user) // 카카오 로그인 API 모듈
    implementation(libs.v2.share) // 카카오톡 공유 API 모듈

    implementation(libs.annotations)

    // 중복된 com.intellij:annotations 라이브러리 제외
    configurations.all {
        exclude(group = "com.intellij", module = "annotations")
    }
}