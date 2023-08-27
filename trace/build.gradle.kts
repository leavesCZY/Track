@file:Suppress("UnstableApiUsage")

plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
    id("com.gradle.plugin-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
    withSourcesJar()
}

group = "io.github.leavesczy"
version = "0.0.3"

gradlePlugin {
    website.set("https://github.com/leavesCZY/Trace")
    vcsUrl.set("https://github.com/leavesCZY/Trace")
    plugins {
        create("TracePlugin") {
            id = "${group}.trace"
            implementationClass = "github.leavesczy.trace.TracePlugin"
            displayName = "Android Developer ASM Gradle Plugin"
            description = "Android Developer ASM Gradle Plugin"
            tags.set(setOf("agp", "asm"))
        }
    }
}

dependencies {
    val agpVersion = "7.4.2"
    val asmVersion = "9.2"
    compileOnly("com.android.tools.build:gradle:${agpVersion}")
    compileOnly("com.android.tools.build:gradle-api:${agpVersion}")
    compileOnly("org.ow2.asm:asm-commons:${asmVersion}")
}