import com.google.devtools.ksp.gradle.KspAATask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.around.library)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "events.around.i18n"
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        val commonJvmAndroidMain = create("commonJvmAndroidMain") {
            dependsOn(commonMain.get())
        }

        androidMain {
            dependsOn(commonJvmAndroidMain)
        }

        jvmMain {
            dependsOn(commonJvmAndroidMain)
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.lyricist.processorXml)
}

ksp {
    arg("lyricist.xml.resourcesPath", "$projectDir/src/commonMain/resources/locale")
    arg("lyricist.packageName", "events.around.i18n")
    arg("lyricist.xml.moduleName", "Around")
    arg("lyricist.xml.defaultLanguageTag", "en")
    arg("lyricist.xml.generateComposeAccessors", "false")
}

tasks.withType<KotlinCompilationTask<*>>().configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

// The platform KSP tasks read commonMain, which includes the generated Lyricist sources below.
tasks.withType<KspAATask>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    } else {
        // Lyricist reads the XML through a processor argument, which Gradle doesn't track: without this an edited
        // strings.xml leaves the generated code stale.
        inputs.dir("src/commonMain/resources/locale")
    }
}

kotlin.sourceSets.commonMain {
    kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
}
