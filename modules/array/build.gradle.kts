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
    js {
        browser()
        nodejs()
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":kodivx-core"))
            implementation(project(":kodivx-buffer"))
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
