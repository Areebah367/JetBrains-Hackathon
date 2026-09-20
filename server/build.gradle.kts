plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // The server reuses the app's own Kotlin model and curated data through the shared module's
    // JVM (desktop) artifact — the same Event class serialises on both sides of the wire.
    implementation(project(":shared"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.contentNegotiation)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.serialization.kotlinxJson)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.logback.classic)
}

application {
    mainClass.set("areebah.nyuad4jetbrains.project.server.ServerKt")
}
