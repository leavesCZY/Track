import github.leavesczy.track.member.MemberFieldRule
import github.leavesczy.track.member.MemberMethodRule
import github.leavesczy.track.superclass.SuperclassRule
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.TimeZone

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.leavesczy.track)
}

android {
    namespace = "github.leavesczy.track"
    compileSdk {
        version = release(version = 37)
    }
    defaultConfig {
        applicationId = "github.leavesczy.track"
        minSdk {
            version = release(version = 23)
        }
        targetSdk {
            version = release(version = 37)
        }
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    signingConfigs {
        create("release") {
            storeFile = File(File(rootDir, "doc"), "key.jks")
            keyAlias = "leavesCZY"
            keyPassword = "123456"
            storePassword = "123456"
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
            enableV4Signing = true
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
    val basePluginExtension = project.extensions.getByType(BasePluginExtension::class.java)
    basePluginExtension.apply {
        val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
        val time = simpleDateFormat.format(Calendar.getInstance().time)
        archivesName.set("track_v${defaultConfig.versionName}_${defaultConfig.versionCode}_${time}")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.value(JvmTarget.JVM_17)
            optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
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
                "**/*.kotlin_module",
                "**/CHANGES",
                "**/LICENSE.txt",
                "**/{AL2.0,LGPL2.1}",
                "**/DebugProbesKt.bin",
                "**/app-metadata.properties",
                "**/kotlin-tooling-metadata.json",
                "**/version-control-info.textproto",
                "**/androidsupportmultidexversion.txt"
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
    clickHandlerClass = "github.leavesczy.track.click.view.ViewClickHandler"
    clickMethodName = "shouldHandleClick"
    skipOnClickAnnotation = "github.leavesczy.track.click.view.SkipViewOnClick"
    include = setOf()
    exclude = setOf()
}

composeClickTrack {
    clickWrapperClass = "github.leavesczy.track.click.compose.ComposeClickWrapper"
    skipOnClickLabel = "skip"
}

superclassTrack {
    rules = setOf(
        SuperclassRule(
            originClass = "github.leavesczy.track.superclass.OriginGreeter",
            targetClass = "github.leavesczy.track.superclass.ProxyGreeter",
            exclude = setOf(".*\\.ExcludedGreeter$")
        ),
        SuperclassRule(
            originClass = "github.leavesczy.track.superclass.OriginLogger",
            targetClass = "github.leavesczy.track.superclass.ProxyLogger",
            include = setOf(".*\\.AppLogger$")
        )
    )
}

memberTrack {
    val toastProxy = "github.leavesczy.track.member.ToastProxy"
    val systemMethodProxy = "github.leavesczy.track.member.SystemMethodProxy"
    val systemFieldProxy = "github.leavesczy.track.member.SystemFieldProxy"
    val echoProxy = "github.leavesczy.track.member.EchoProxy"
    val memberTrackInclude = setOf(".*\\.MemberTrackActivity$")
    methods = setOf(
        MemberMethodRule(
            ownerClass = "android.widget.Toast",
            methodName = "show",
            methodDescriptor = "()V",
            proxyClass = toastProxy,
            include = memberTrackInclude
        ),
        MemberMethodRule(
            ownerClass = $$"android.provider.Settings$Secure",
            methodName = "getString",
            methodDescriptor = "(Landroid/content/ContentResolver;Ljava/lang/String;)Ljava/lang/String;",
            proxyClass = systemMethodProxy,
            include = memberTrackInclude
        ),
        MemberMethodRule(
            ownerClass = "github.leavesczy.track.member.Echo",
            methodName = "echo",
            methodDescriptor = MemberMethodRule.MATCH_ALL_METHOD_DESCRIPTORS,
            proxyClass = echoProxy,
            include = memberTrackInclude
        )
    )
    fields = setOf(
        MemberFieldRule(
            ownerClass = "android.os.Build",
            fieldName = "BRAND",
            typeDescriptor = "Ljava/lang/String;",
            proxyClass = systemFieldProxy,
            include = memberTrackInclude
        )
    )
}