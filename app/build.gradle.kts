plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile = file("debug.keystore.old")
            storePassword = "android"
            keyPassword = "android"
            keyAlias = "androiddebugkey"
        }
        create("release") {
            storeFile = file("debug.keystore.old")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
    namespace = "com.izontechnology.dcapp"
    compileSdk = 34
    flavorDimensions.add("default")

    defaultConfig {
        applicationId = "com.izontechnology.dcapp"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
//        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", "\"https://dcapi.izontechnology.com\"")
            buildConfigField("String", "API_VERSION", "\"\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    productFlavors {
        create("prod") {
            buildConfigField("String", "API_BASE_URL", "\"https://dcapi.izontechnology.com\"")
            buildConfigField("String", "API_VERSION", "\"\"")
            versionName = "1.1.1"
            setProperty("archivesBaseName", "Prod IZON DC -V $versionName")
        }
        create("dev") {
            buildConfigField("String", "API_BASE_URL", "\"https://devdcapi.izontechnology.com\"")
            buildConfigField("String", "API_VERSION", "\"\"")
            versionName = "1.4.6"
            setProperty("archivesBaseName", "IZON DC -V $versionName")
        }

    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.things:androidthings:1.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Navigation Components
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // View Model
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")

    //preferences
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-compiler:2.51.1")

    // hilt workmanager
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // Retrofit
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Interceptor
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:5.0.0-alpha.10")
    implementation("com.squareup.okhttp3:okhttp-urlconnection:4.4.1")

    //ssp and sdp
    implementation("com.intuit.ssp:ssp-android:1.0.6")
    implementation("com.intuit.sdp:sdp-android:1.0.6")

    //circular view
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //enable GPS
    implementation("com.google.android.gms:play-services-location:21.2.0")

    implementation("io.github.chaosleung:pinview:1.4.4")

    implementation("com.github.avnet-iotconnect:iotc-android-sdk:3.1.4") {
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-media-compat")
    }
}

kapt {
    correctErrorTypes = true
}