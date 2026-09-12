plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

allprojects {
    group = "io.kodivx"
    version = "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}