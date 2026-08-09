plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ksp)
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    implementation(projects.sharedBase)

    implementation(libs.ksp.symbol.processing)
    implementation(libs.kotlinpoet)
    implementation(libs.kotlinpoet.ksp)
}