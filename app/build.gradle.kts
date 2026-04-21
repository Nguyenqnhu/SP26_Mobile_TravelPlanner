plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.weathertrip_sep490"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.weathertrip_sep490"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Fallback default, specific buildTypes below will override this value.
        buildConfigField("String", "API_BASE_URL", "\"https://travelplanner-e8afamefddf8bwc7.southeastasia-01.azurewebsites.net/\"")
    }

    buildTypes {
        debug {
            // Cloud backend for development/testing
            buildConfigField("String", "API_BASE_URL", "\"https://travelplanner-e8afamefddf8bwc7.southeastasia-01.azurewebsites.net/\"")
        }
        release {
            // Cloud backend for production
            buildConfigField("String", "API_BASE_URL", "\"https://travelplanner-e8afamefddf8bwc7.southeastasia-01.azurewebsites.net/\"")
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image loading (URL -> ImageView)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0")

    implementation("com.google.android.gms:play-services-maps:19.1.0")

    implementation("androidx.viewpager2:viewpager2:1.1.0")

}