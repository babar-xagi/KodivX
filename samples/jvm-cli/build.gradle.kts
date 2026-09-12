plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":kodivx-core"))
    implementation(project(":kodivx-buffer"))
    implementation(project(":kodivx-array"))
    implementation(project(":kodivx-frame"))
    testImplementation(kotlin("test"))
}

application {
    mainClass.set("io.kodivx.samples.cli.MainKt")
}
