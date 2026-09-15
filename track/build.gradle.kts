import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.gradle.plugin.publish)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.value(JvmTarget.JVM_17)
    }
}

group = "io.github.leavesczy"
version = "1.1.7"

gradlePlugin {
    website.set("https://github.com/leavesCZY/Track")
    vcsUrl.set("https://github.com/leavesCZY/Track")
    plugins {
        create("TrackPlugin") {
            id = "${group}.track"
            implementationClass = "github.leavesczy.track.TrackPlugin"
            displayName = "Android Developer Gradle Plugin"
            description = "Android Developer Gradle Plugin"
            tags.set(setOf("agp"))
        }
    }
}

dependencies {
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.ow2.asm.tree)
}