plugins {
    alias(libs.plugins.around.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "events.around.core"
        withHostTest {}
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                api(libs.kotlinx.date.time)
                api(libs.immutable.collections)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        matching { it.name.startsWith("ios") }.all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }
}
