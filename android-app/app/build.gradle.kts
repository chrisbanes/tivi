// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0


import com.android.build.gradle.internal.tasks.factory.dependsOn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

plugins {
    id("app.tivi.android.application")
    id("app.tivi.kotlin.android")
    id("app.tivi.compose")
    alias(libs.plugins.licensee)
    alias(libs.plugins.ksp)
}

val appVersionCode = properties["TIVI_VERSIONCODE"]?.toString()?.toInt() ?: 1000
println("APK version code: $appVersionCode")

val useReleaseKeystore = rootProject.file("release/app-release.jks").exists()

android {
    namespace = "app.tivi"

    defaultConfig {
        applicationId = "app.tivi"
        versionCode = appVersionCode
        versionName = "0.9.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("release/app-debug.jks")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }

        create("release") {
            if (useReleaseKeystore) {
                storeFile = rootProject.file("release/app-release.jks")
                storePassword = properties["TIVI_RELEASE_KEYSTORE_PWD"]?.toString() ?: ""
                keyAlias = "tivi"
                keyPassword = properties["TIVI_RELEASE_KEY_PWD"]?.toString() ?: ""
            }
        }
    }

    lint {
        baseline = file("lint-baseline.xml")
        // Disable lintVital. Not needed since lint is run on CI
        checkReleaseBuilds = false
        // Ignore any tests
        ignoreTestSources = true
        // Make the build fail on any lint errors
        abortOnError = true
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources.excludes += setOf(
            // Exclude AndroidX version files
            "META-INF/*.version",
            // Exclude consumer proguard files
            "META-INF/proguard/*",
            // Exclude the Firebase/Fabric/other random properties files
            "/*.properties",
            "fabric/*.properties",
            "META-INF/*.properties",
            // License files
            "LICENSE*",
            // Exclude Kotlin unused files
            "META-INF/**/previous-compilation-data.bin",
        )
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs["debug"]
            versionNameSuffix = "-dev"
            applicationIdSuffix = ".debug"
        }

        release {
            signingConfig = signingConfigs[if (useReleaseKeystore) "release" else "debug"]
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }

        create("benchmark") {
            initWith(buildTypes["release"])
            signingConfig = signingConfigs["debug"]
            matchingFallbacks += "release"
            proguardFiles("benchmark-rules.pro")
        }
    }

    flavorDimensions += "mode"
    productFlavors {
        create("qa") {
            dimension = "mode"
            // This is a build with Chucker enabled
            proguardFiles("proguard-rules-chucker.pro")
            versionNameSuffix = "-qa"
        }

        create("standard") {
            dimension = "mode"
            // Standard build is always ahead of the QA builds as it goes straight to
            // the alpha channel. This is the 'release' flavour
            versionCode = (android.defaultConfig.versionCode ?: 0) + 1
        }
    }

    testOptions {
        managedDevices {
            devices {
                create<com.android.build.api.dsl.ManagedVirtualDevice>("api31") {
                    device = "Pixel 6"
                    apiLevel = 31
                    systemImageSource = "aosp"
                }
            }
        }
    }
}

androidComponents {
    // Ignore the QA Benchmark variant
    val qaBenchmark = selector()
        .withBuildType("benchmark")
        .withFlavor("mode" to "qa")
    beforeVariants(qaBenchmark) { variant ->
        variant.enable = false
    }

    // Ignore the standardDebug variant
    val standard = selector()
        .withBuildType("debug")
        .withFlavor("mode" to "standard")
    beforeVariants(standard) { variant ->
        variant.enable = false
    }
}

dependencies {
    qaImplementation(projects.shared.qa)
    standardImplementation(projects.shared.prod)

    implementation(libs.androidx.activity.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.splashscreen)

    qaImplementation(libs.leakCanary)

    implementation(libs.kotlin.coroutines.android)

    implementation(libs.google.firebase.crashlytics)

    androidTestImplementation(projects.androidApp.commonTest)
    androidTestImplementation(libs.androidx.uiautomator)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.test.rules)
}

if (file("google-services.json").exists()) {
    apply(plugin = libs.plugins.gms.googleServices.get().pluginId)
    apply(plugin = libs.plugins.firebase.crashlytics.get().pluginId)

    // Disable uploading mapping files for the benchmark build type
    android.buildTypes.getByName("benchmark") {
        configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
            mappingFileUploadEnabled = false
        }
    }
}

abstract class GenerateLicensesAsset : DefaultTask() {
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    abstract val buildDir: DirectoryProperty

    @get:OutputFile abstract val jsonFile: RegularFileProperty

    private val licenseeFile: File
        get() = File(buildDir.asFile.get(), "reports/licensee/qaDebug/artifacts.json")

    @Suppress("UNCHECKED_CAST")
    @OptIn(ExperimentalStdlibApi::class)
    @TaskAction
    fun generate() {
        val json = Json { ignoreUnknownKeys = true }
        val fileContent = licenseeFile.readText()
        val licenseJsonList = json.decodeFromString<List<JsonObject>>(fileContent)
        val sortedLicenseJsonList = licenseJsonList
            .sortedBy { it["name"].toString().lowercase() }
        val jsonString = json.encodeToString(sortedLicenseJsonList)
        jsonFile.get().asFile.writeText(jsonString)
    }
}

val generateLicenseTask =
    tasks.register<GenerateLicensesAsset>("generateLicensesAsset") {
        buildDir.set(project.layout.buildDirectory)
        jsonFile.set(project.layout.projectDirectory.file("src/main/assets/generated_licenses.json"))
    }

generateLicenseTask.dependsOn("licenseeQaDebug")

licensee {
    allow("Apache-2.0")
    allow("MIT")
    allow("BSD-3-Clause")
    allowUrl("https://developer.android.com/studio/terms.html")
}

fun <T : Any> propOrDef(propertyName: String, defaultValue: T): T {
    @Suppress("UNCHECKED_CAST")
    val propertyValue = project.properties[propertyName] as T?
    return propertyValue ?: defaultValue
}

fun DependencyHandler.qaImplementation(dependencyNotation: Any) =
    add("qaImplementation", dependencyNotation)

fun DependencyHandler.standardImplementation(dependencyNotation: Any) =
    add("standardImplementation", dependencyNotation)
