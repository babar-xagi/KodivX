plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

kotlin {
    jvm {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
        }
    }
    
    // Native targets
    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()
    
    // iOS targets
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    // Web targets
    js(IR) {
        browser()
        nodejs()
    }
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kodivx-core"))
            implementation(project(":kodivx-buffer"))
            implementation(project(":kodivx-array"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
