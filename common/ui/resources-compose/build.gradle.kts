plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.cacheFixPlugin)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "dev.icerock.moko.resources.compose"

    buildFeatures {
        buildConfig = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composecompiler.get()
    }
}

dependencies {
    api(platform(libs.compose.bom))
    implementation(libs.compose.foundation.foundation)

    api("dev.icerock.moko:resources:0.22.0")
}
