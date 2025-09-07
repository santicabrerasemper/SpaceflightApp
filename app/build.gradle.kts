plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.navigation.safeargs)
}

android {
    namespace = "com.example.spaceflightapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.spaceflightapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    //Coil
    implementation(libs.coil)

    //Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.logging.interceptor)

    // Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //Paging
    implementation(libs.androidx.paging.runtime)

    //Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)

    //RecyclerView
    implementation (libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.swiperefreshlayout)

    // DI (Koin)
    implementation(libs.koin.android)

    //Logs
    implementation(libs.timber)

    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    //Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation (libs.mockwebserver)
    testImplementation (libs.kotlinx.coroutines.test)
    testImplementation (libs.converter.moshi.v2110)
    testImplementation (libs.moshi.kotlin.v1151)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}