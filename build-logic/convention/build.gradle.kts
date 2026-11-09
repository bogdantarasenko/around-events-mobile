import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.detekt)
}

gradlePlugin {
    plugins {
        register("library") {
            id = "events.around.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("detekt") {
            id = "events.around.detekt"
            implementationClass = "DetektConventionPlugin"
        }
        register("versioning") {
            id = "events.around.versioning"
            implementationClass = "VersioningConventionPlugin"
        }
    }
}
