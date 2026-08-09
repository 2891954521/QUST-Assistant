import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidMultiplatformLibrary)

    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)

    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)

    alias(libs.plugins.ksp)
}

val env: String = run {
    val e = project.findProperty("env") as? String
    if(!e.isNullOrEmpty()) return@run e
    val taskNames = gradle.startParameter.taskNames
    return@run when {
        taskNames.any { it.contains("Debug", true) || it.contains("run", true) } -> "dev"
        taskNames.any { it.contains("Release", true) } -> "prod"
        else -> "prod"
    }
}

val generateConfig by tasks.register<GenerateConfigTask>("generateConfig") {

    packageName = "com.qust.helper.next"

    className = "AppConfig"

    val f = rootProject.file("config/$env.properties").takeIf { it.exists() } ?: rootProject.file("config/prod.properties")

    inputFile = layout.projectDirectory.file(f.path)
    outputDir = layout.buildDirectory.dir("generated/source/config")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    jvmToolchain(11)

    jvm()

    android {
       namespace = "com.qust.helper.next.shared"
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
    }
    
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateConfig)

            dependencies {
                implementation(projects.sharedBase)

                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)

                implementation(libs.androidx.lifecycle.viewmodelCompose)
                implementation(libs.androidx.lifecycle.runtimeCompose)

                implementation(libs.androidx.navigation.compose)

                implementation(libs.androidx.material.icons)
                implementation(libs.androidx.material3.windowSizeClass)

                implementation(libs.kotlin.reflect)
                implementation(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.ktor.client.core)
                implementation(libs.ktor.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                implementation(libs.third.multiplatform.settings)
            }
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        val androidMain by getting {
            dependencies {

                implementation(fileTree("src/androidMain/libs"))

                implementation(libs.ktor.client.okhttp)

                implementation(libs.androidx.activity.compose)
                implementation(libs.compose.uiToolingPreview)
            }
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)

            implementation(libs.jvm.third.log4j)

            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

dependencies {
    add("kspJvm", projects.sharedKsp)
    add("kspAndroid", projects.sharedKsp)

    androidRuntimeClasspath(libs.compose.uiTooling)
}
