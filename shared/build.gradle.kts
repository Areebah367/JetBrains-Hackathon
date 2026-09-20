import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

// The Ticketmaster API key comes from the TICKETMASTER_API_KEY environment variable, or from
// `ticketmaster.apiKey` in local.properties (gitignored). It is written into a generated Kotlin
// file in the build folder, so it never lives in the repo. With no key, the app uses sample events.
val ticketmasterApiKey: Provider<String> = providers.environmentVariable("TICKETMASTER_API_KEY")
    .orElse(
        providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
            .asText
            .map { text ->
                text.lineSequence()
                    .map { it.trim() }
                    .firstOrNull { it.startsWith("ticketmaster.apiKey=") }
                    ?.substringAfter("=")
                    ?.trim()
                    .orEmpty()
            }
    )
    .orElse("")
    .map { it.trim() }

val secretsDir = layout.buildDirectory.dir("generated/secrets/commonMain/kotlin")

val generateSecrets = tasks.register("generateSecrets") {
    val outputDir = secretsDir
    val apiKey = ticketmasterApiKey
    inputs.property("ticketmasterApiKey", apiKey)
    outputs.dir(outputDir)
    outputs.doNotCacheIf("The generated file contains an API key") { true }
    doLast {
        val key = apiKey.get()
        require(Regex("[A-Za-z0-9_-]*").matches(key)) { "The Ticketmaster API key has unexpected characters" }
        val file = outputDir.get().file("areebah/nyuad4jetbrains/project/Secrets.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            "package areebah.nyuad4jetbrains.project\n\n" +
                "internal object Secrets {\n" +
                "    const val TICKETMASTER_API_KEY: String = \"$key\"\n" +
                "}\n"
        )
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
       namespace = "areebah.nyuad4jetbrains.project.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()

       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
       withDeviceTestBuilder {
           sourceSetTreeName = "test"
       }.configure {
           instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
       }
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(project.files(secretsDir).builtBy(generateSecrets))
        }

        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}
