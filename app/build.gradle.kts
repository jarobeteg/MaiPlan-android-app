plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.maiplan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.maiplan"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
        compose = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    flavorDimensions += "environment"

    productFlavors {

        create("dev") {
            dimension = "environment"

            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://100.70.156.115:8001/\""
            )

            buildConfigField(
                "String",
                "API_HOST",
                "\"100.70.156.115\""
            )

            buildConfigField(
                "int",
                "API_PORT",
                "8001"
            )
        }

        create("integration") {
            dimension = "environment"

            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://100.70.156.115:8002/\""
            )

            buildConfigField(
                "String",
                "API_HOST",
                "\"100.70.156.115\""
            )

            buildConfigField(
                "int",
                "API_PORT",
                "8002"
            )
        }

        create("prod") {
            dimension = "environment"

            buildConfigField(
                "String",
                "API_BASE_URL",
                "\"http://100.70.156.115:8000/\""
            )

            buildConfigField(
                "String",
                "API_HOST",
                "\"100.70.156.115\""
            )

            buildConfigField(
                "int",
                "API_PORT",
                "8000"
            )
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.auth0.jwt)
    implementation(libs.bcrypt)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.navigation.compose)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.material3.windowSizeClass)
    testImplementation(libs.junit)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.activity)
    implementation(libs.material.icons.extended)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.compose.foundation)
    implementation(libs.androidx.work.runtime.ktx)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    ksp(libs.room.compiler)
}