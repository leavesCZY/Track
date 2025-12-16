import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import github.leavesczy.track.replace.instruction.ReplaceInstruction
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.leavesczy.track)
}

android {
    namespace = "github.leavesczy.track"
    compileSdk = 36
    defaultConfig {
        applicationId = "github.leavesczy.track"
        minSdk = 23
        targetSdk = 36
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
            if (this is ApkVariantOutputImpl) {
                val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
                simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
                val time = simpleDateFormat.format(Calendar.getInstance().time)
                this.outputFileName =
                    "track_${variant.name}_v${variant.versionName}_${variant.versionCode}_${time}.apk"
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.value(JvmTarget.JVM_11)
        }
    }
    buildFeatures {
        compose = true
    }
    packaging {
        jniLibs {
            excludes += setOf("META-INF/{AL2.0,LGPL2.1}")
        }
        resources {
            excludes += setOf(
                "**/*.md",
                "**/*.version",
                "**/*.properties",
                "**/**/*.properties",
                "META-INF/{AL2.0,LGPL2.1}",
                "META-INF/CHANGES",
                "META-INF/kotlin-stdlib.kotlin_module",
                "META-INF/mediationsdk_release.kotlin_module",
                "META-INF/unity-ads_release.kotlin_module",
                "DebugProbesKt.bin",
                "kotlin-tooling-metadata.json",
                "androidsupportmultidexversion.txt"
            )
        }
    }
}

dependencies {
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.google.material)
}

viewClickTrack {
    isEnabled = true
    include = setOf()
    exclude = setOf()
    onClickClass = "github.leavesczy.track.click.view.ViewClickMonitor"
    onClickMethodName = "isEnabled"
    uncheckViewOnClickAnnotation = "github.leavesczy.track.click.view.UncheckViewOnClick"
}

composeClickTrack {
    isEnabled = true
    onClickClass = "github.leavesczy.track.click.compose.ComposeOnClick"
    onClickWhiteList = "notCheck"
}

toastTrack {
    isEnabled = true
    include = setOf()
    exclude = setOf()
    proxyOwner = "github.leavesczy.track.toast.ToastProxy"
}

optimizedThreadTrack {
    isEnabled = true
    include = setOf()
    exclude = setOf()
    proxyOwner = "github.leavesczy.track.thread.OptimizedExecutors"
    methods = setOf(
        "newSingleThreadExecutor",
        "newCachedThreadPool",
        "newFixedThreadPool",
        "newScheduledThreadPool",
        "newSingleThreadScheduledExecutor"
    )
}

replaceClassTrack {
    isEnabled = true
    include = setOf()
    exclude = setOf(".*\\.IgnoreImageView\$")
    originClass = "android.widget.ImageView"
    targetClass = "github.leavesczy.track.replace.clazz.MonitorImageView"
}

replaceFieldTrack {
    isEnabled = true
    include = setOf()
    exclude = setOf()
    instructions = setOf(
        ReplaceInstruction(
            owner = "android.os.Build",
            name = "BRAND",
            descriptor = "Ljava/lang/String;",
            proxyOwner = "github.leavesczy.track.replace.instruction.SystemFieldProxy"
        )
    )
}

replaceMethodTrack {
    isEnabled = true
    include = setOf()
    exclude = setOf()
    val systemMethodProxyOwner = "github.leavesczy.track.replace.instruction.SystemMethodProxy"
    instructions = setOf(
        ReplaceInstruction(
            owner = "android.telephony.TelephonyManager",
            name = "getDeviceId",
            descriptor = "()Ljava/lang/String;",
            proxyOwner = systemMethodProxyOwner
        ),
        ReplaceInstruction(
            owner = "android.telephony.TelephonyManager",
            name = "getImei",
            descriptor = "(I)Ljava/lang/String;",
            proxyOwner = systemMethodProxyOwner
        ),
        ReplaceInstruction(
            owner = "android.provider.Settings\$Secure",
            name = "getString",
            descriptor = "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;",
            proxyOwner = systemMethodProxyOwner
        )
    )
}