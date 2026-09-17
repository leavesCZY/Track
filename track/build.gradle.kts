plugins {
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.gradle.plugin.publish)
}

kotlin {
    jvmToolchain(17)
}

java {
    withJavadocJar()
    withSourcesJar()
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
            displayName = "Track"
            description =
                "Android ASM instrumentation for click debounce, superclass rewriting, and field/method member replacement."
            tags.set(setOf("android", "agp", "asm", "bytecode", "instrumentation"))
        }
    }
}

dependencies {
    compileOnly(libs.android.gradle.api)
    compileOnly(libs.ow2.asm.tree)
}