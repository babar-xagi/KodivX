pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "kodivx"

include(":kodivx-core")
project(":kodivx-core").projectDir = file("modules/core")

include(":kodivx-buffer")
project(":kodivx-buffer").projectDir = file("modules/buffer")

include(":kodivx-array")
project(":kodivx-array").projectDir = file("modules/array")

include(":kodivx-frame")
project(":kodivx-frame").projectDir = file("modules/frame")

include(":samples:jvm-cli")
project(":samples:jvm-cli").projectDir = file("samples/jvm-cli")