import co.touchlab.skie.configuration.SuppressSkieWarning
import co.touchlab.skie.configuration.SuspendInterop
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.around.detekt)
    alias(libs.plugins.skie)
}

kotlin {
    jvmToolchain(21)

    android {
        namespace = "events.around.shared"
        compileSdk = libs.versions.android.compile.sdk.get().toInt()
        minSdk = libs.versions.android.min.sdk.get().toInt()
        compilerOptions.jvmTarget = JvmTarget.JVM_21

        withHostTest {}
    }

    // JVM target only for fast compile checks and unit tests; there is no desktop app.
    jvm()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
        // Simulator on Intel Macs.
        iosX64(),
    ).forEach {
        it.binaries.framework {
            baseName = "AroundKit"
            isStatic = true
            binaryOption("bundleId", "events.around.AroundKit")

            export(project.dependencies.project(":i18n"))
            export(project.dependencies.project(":core"))
            export(libs.touchlab.kermit.simple)
            export(libs.androidx.lifecycle.viewModel)
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
            languageSettings.optIn("kotlin.time.ExperimentalTime")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
        }

        matching { it.name.startsWith("ios") }.all {
            languageSettings.optIn("com.russhwolf.settings.ExperimentalSettingsImplementation")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }

        commonMain {
            dependencies {
                implementation(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.core.vm)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.multiplatform.settings)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization)
                implementation(libs.ktor.logging)
                implementation(libs.skie.annotation)

                api(project(":core"))
                api(project(":i18n"))
                api(libs.touchlab.kermit)
                api(libs.androidx.lifecycle.viewModel)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.koin.test)
                implementation(libs.turbine)
                implementation(libs.multiplatform.settings.test)
                implementation(libs.ktor.client.mock)
            }
        }

        val commonJvmAndroidMain = create("commonJvmAndroidMain") {
            dependsOn(commonMain.get())

            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        androidMain {
            dependsOn(commonJvmAndroidMain)

            dependencies {
                implementation(libs.koin.android)
            }
        }

        getByName("androidHostTest") {
            dependencies {
                implementation(libs.junit)
            }
        }

        iosMain {
            dependencies {
                api(libs.touchlab.kermit.simple)
                implementation(libs.ktor.client.darwin)
            }
        }

        jvmMain {
            dependsOn(commonJvmAndroidMain)
        }
    }
}

skie {
    features {
        group {
            SuspendInterop.Enabled(false)
        }
        // Third-party types whose `description` collides with NSObject.description; Swift never uses them.
        group("io.ktor.http.HttpStatusCode") {
            SuppressSkieWarning.NameCollision(true)
        }
    }
}
