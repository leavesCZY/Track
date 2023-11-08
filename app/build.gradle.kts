@file:Suppress("UnstableApiUsage")

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.leavesczy.trace)
}

android {
    namespace = "github.leavesczy.trace"
    compileSdk = 34
    defaultConfig {
        applicationId = "github.leavesczy.trace"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile =
                File(rootDir.absolutePath + File.separator + "doc" + File.separator + "key.jks")
            keyAlias = "leavesCZY"
            keyPassword = "123456"
            storePassword = "123456"
            enableV1Signing = true
            enableV2Signing = true
        }
    }
    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    applicationVariants.all {
        val variant = this
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
                simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
                val time = simpleDateFormat.format(Calendar.getInstance().time)
                this.outputFileName =
                    "trace_${variant.name}_versionCode_${variant.versionCode}_versionName_${variant.versionName}_${time}.apk"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    packaging {
        resources {
            excludes += setOf(
                "**/*.md",
                "**/*.version",
                "**/*.properties",
                "**/**/*.properties",
                "META-INF/{AL2.0,LGPL2.1}",
                "META-INF/CHANGES",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json"
            )
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    testImplementation(libs.test.junit)
    androidTestImplementation(libs.test.androidx.junit)
    androidTestImplementation(libs.test.androidx.espresso)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.google.material)
    val composePlatform = platform(libs.compose.bom)
    implementation(composePlatform)
    implementation(libs.compose.material3)
}

viewClickTrace {
    onClickClass = "github.leavesczy.trace.click.view.ViewClickMonitor"
    onClickMethodName = "isEnabled"
    uncheckViewOnClickAnnotation = "github.leavesczy.trace.click.view.UncheckViewOnClick"
    include = setOf()
    exclude = setOf()
}

composeClickTrace {
    onClickClass = "github.leavesczy.trace.click.compose.ComposeOnClick"
    onClickWhiteList = "notCheck"
}

replaceClassTrace {
    originClass = "android.widget.ImageView"
    targetClass = "github.leavesczy.trace.replace.MonitorImageView"
    include = setOf()
    exclude = setOf(".*\\.IgnoreImageView\$")
}

toastTrace {
    toasterClass = "github.leavesczy.trace.toast.Toaster"
    showToastMethodName = "showToast"
}