import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)

    kotlin("plugin.serialization") version "2.0.0"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    
//    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
//        iosTarget.binaries.framework {
//            baseName = "ComposeApp"
//            isStatic = true
//        }
//    }
    
    jvm("desktop")
    
//    @OptIn(ExperimentalWasmDsl::class)
//    wasmJs {
//        moduleName = "composeApp"
//        browser {
//            val rootDirPath = project.rootDir.path
//            val projectDirPath = project.projectDir.path
//            commonWebpackConfig {
//                outputFileName = "composeApp.js"
//                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
//                    static = (static ?: mutableListOf()).apply {
//                        // Serve sources to debug inside browser
//                        add(rootDirPath)
//                        add(projectDirPath)
//                    }
//                }
//            }
//        }
//        binaries.executable()
//    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.lifecycle.runtime.compose)

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

            implementation(libs.ktor.client.core)

            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

            implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
        }

        androidMain.dependencies {
            implementation(libs.mmkv)
            implementation(libs.ktor.client.okhttp)

            implementation("androidx.core:core-ktx:1.12.0")
            implementation("androidx.compose.foundation:foundation-layout")
            implementation("androidx.compose.ui:ui-util")
            implementation("androidx.compose.ui:ui-graphics")

            implementation("androidx.glance:glance:1.0.0")
            implementation("androidx.glance:glance-appwidget:1.0.0")

            implementation("androidx.appcompat:appcompat:1.6.1")

            implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
            implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
            implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

            implementation("androidx.navigation:navigation-compose:2.7.7")

            implementation("androidx.work:work-runtime-ktx:2.9.0")

            implementation("androidx.room:room-runtime:2.6.1")

            implementation("org.jsoup:jsoup:1.12.1")

            implementation("com.umeng.umsdk:common:9.6.8")
            implementation("com.umeng.umsdk:asms:1.8.2")
            implementation("com.umeng.umsdk:apm:1.2.0")

            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }

//        iosMain.dependencies {
//            implementation(libs.ktor.client.darwin)
//        }

        val desktopMain by getting
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "com.qust.helper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.qust.helper"
        minSdk = 21
        targetSdk = 34
        versionCode = 15
        versionName = "v3.6.0127"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resourceConfigurations.clear()
        resourceConfigurations += arrayOf("zh", "zh-rCN")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi", "armeabi-v7a", "x86", "x86_64")
        }
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "PACKAGE_TIME", "\"${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())}\"")
        buildConfigField("String", "UMENG_APP_KEY", "\"\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("String", "UMENG_APP_CHANNEL", "\"Release\"")
        }
        debug {
            buildConfigField("String", "UMENG_APP_CHANNEL", "\"Test\"")
        }
    }

    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName = "QustHelper_${defaultConfig.versionName}.apk"
            }
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.qust.helper"
    generateResClass = auto
}

compose.desktop {
    application {
        mainClass = "com.qust.helper.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.qust.helper"
            packageVersion = "1.0.0"
        }
    }
}
