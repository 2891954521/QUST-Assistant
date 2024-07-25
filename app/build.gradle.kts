import groovy.json.JsonSlurper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
	id("com.android.application")
	id("org.jetbrains.kotlin.android")
	kotlin("plugin.serialization") version "1.9.10"
	kotlin("kapt")
}

android {
	namespace = "com.qust.helper"
	compileSdk = 34

	defaultConfig {
		applicationId = "com.qust.helper"
		minSdk = 21
		targetSdk = 34
		versionCode = 13
		versionName = "v3.5.0725"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

		resourceConfigurations.clear()
		resourceConfigurations += arrayOf("zh", "zh-rCN")

		ndk {
			abiFilters += listOf("armeabi","armeabi-v7a", "x86") // 'x86_64', 'mips', 'mips64'
		}
		vectorDrawables {
			useSupportLibrary = true
		}

		buildConfigField("String", "PACKAGE_TIME", "\"${SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date())}\"")

		val file = File("${rootProject.projectDir.path}/config.json")
		if(file.exists()){
			val configString = file.readText()
			val jsonArray = JsonSlurper().parseText(configString) as Map<String, Any>
			buildConfigField("String", "UMENG_APP_KEY", "\"${jsonArray["umeng_app_key"].toString()}\"")
		}else{
			buildConfigField("String", "UMENG_APP_KEY", "\"\"")
		}

		// 指定room.schemaLocation生成的文件路径
//		javaCompileOptions {
//			annotationProcessorOptions {
//				arguments["room.schemaLocation"] = "${rootProject.projectDir.path}/app/build/schemas"
//			}
//		}
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
		val buildType = this.buildType.name
		outputs.all {
			if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
				this.outputFileName = "QustHelper_${defaultConfig.versionName}.apk"
			}
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_1_8
		targetCompatibility = JavaVersion.VERSION_1_8
	}
	kotlinOptions {
		jvmTarget = "1.8"
	}
	buildFeatures {
		compose = true
		buildConfig = true
	}
	composeOptions {
		kotlinCompilerExtensionVersion = "1.5.1"
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
}

dependencies {

	implementation("androidx.compose:compose-bom:2024.04.00")
	androidTestImplementation("androidx.compose:compose-bom:2024.04.00")

	implementation("androidx.core:core-ktx:1.12.0")
	implementation("androidx.compose.runtime:runtime")
	implementation("androidx.compose.foundation:foundation:1.6.5")
	implementation("androidx.compose.foundation:foundation-layout")
	implementation("androidx.compose.ui:ui-util")
	implementation("androidx.compose.ui:ui-graphics")
	implementation("androidx.compose.ui:ui-tooling-preview")
	implementation("androidx.compose.material:material:1.6.5")
	implementation("androidx.compose.material3:material3:1.2.1")

	implementation("androidx.glance:glance:1.0.0")
	implementation("androidx.glance:glance-appwidget:1.0.0")

	implementation("androidx.appcompat:appcompat:1.6.1")

	implementation("androidx.activity:activity-compose:1.8.2")

	implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
	implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
	implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")

	implementation("androidx.navigation:navigation-compose:2.7.7")

	implementation("androidx.work:work-runtime-ktx:2.9.0")

	implementation("androidx.room:room-runtime:2.6.1")
	kapt("androidx.room:room-compiler:2.6.1")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

	implementation("com.squareup.okhttp3:okhttp:4.12.0")
	implementation("org.jsoup:jsoup:1.12.1")

	implementation("com.umeng.umsdk:common:9.6.8")
	implementation("com.umeng.umsdk:asms:1.8.2")
	implementation("com.umeng.umsdk:apm:1.2.0")

	testImplementation("junit:junit:4.13.2")

	androidTestImplementation("androidx.test.ext:junit:1.1.5")
	androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
//	androidTestImplementation("androidx.compose.ui:ui-test-junit4")

	debugImplementation("androidx.compose.ui:ui-tooling")
	debugImplementation("androidx.compose.ui:ui-test-manifest")
}