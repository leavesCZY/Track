@file:Suppress("UnstableApiUsage")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.gradle.plugin.publish)
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

group = "io.github.leavesczy"
version = "0.0.1-beta01"

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
    compileOnly(libs.android.gradle)
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.ow2.asm.commons)
}