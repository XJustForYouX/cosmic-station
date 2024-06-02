@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "emu.cosmic"
    compileSdk = 34

    defaultConfig {
        applicationId = "emu.cosmic"
        minSdk = 31

        targetSdk = 34
        versionCode = 18
        versionName = "0.0.18"
        ndk {
            abiFilters.clear()
            abiFilters.add("arm64-v8a")
        }
    }

    buildTypes {
        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = false
            externalNativeBuild {
                cmake {
                    arguments += listOf("-DCMAKE_BUILD_TYPE=Release", "-DANDROID_STL=c++_shared")
                }
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("debug")
        }

        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            externalNativeBuild {
                cmake {
                    arguments += listOf("-DCMAKE_BUILD_TYPE=Debug", "-DANDROID_STL=c++_shared")
                }
            }
        }
        flavorDimensions += "version"
        productFlavors {
            create("prod") {
                dimension = "version"
                manifestPlaceholders += mutableMapOf("appLabel" to "Cosmic")
            }
            create("dev") {
                dimension = "version"
                applicationIdSuffix = ".dev"
                versionNameSuffix = "-dev"
                manifestPlaceholders += mutableMapOf("appLabel" to "Cosmic Dev")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    ndkVersion = "26.1.10909125"
    externalNativeBuild {
        cmake {
            path = file("src/main/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    buildFeatures {
        viewBinding = true
    }
    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.google.dagger:hilt-android:2.51.1")
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")
}

kapt {
    correctErrorTypes = true
}

/**
 * Returns the version name based on the current git state
 * If HEAD is a tag, the tag name is used as the version name
 * e.g. `1.0.0`
 * If HEAD is not a tag, the closest tag name, the branch name and the short commit hash are used
 * e.g. `1.0.0-master-ab00cd11`
 * If PR_NUMBER is set, prPR_NUMBER is used instead of the branch name
 * e.g. `1.0.0-pr123-ab00cd11`
 */
def getGitVersionName() {
    def versionName = '0.0.0'

    try {
        // Check if HEAD is a tag
        def process = 'git describe --exact-match'.execute([], project.rootDir)
        def isTag = process.waitFor() == 0

        // Use the tag name as the version name
        def tag = 'git describe --abbrev=0'.execute([], project.rootDir).text.trim()
        if (!tag.isEmpty())
            versionName = tag

        // If HEAD is not a tag, append the branch name and the short commit hash
        if (!isTag)
            versionName += '-' + getGitBranch() + '-' + getGitShortHash()
    } catch (Exception e) {
        logger.quiet(e.toString() + ': defaulting to dummy version number ' + versionName)
    }

    logger.quiet('Version name: ' + versionName)
    return versionName
}

/**
 * Returns the number of commits until the last tag
 */
def getGitVersionCode() {
    def versionCode = 1

    try {
        versionCode = Integer.max('git rev-list --first-parent --count --tags'.execute([], project.rootDir).text
                .toInteger(), versionCode)
    } catch (Exception e) {
        logger.error(e.toString() + ': defaulting to dummy version code ' + versionCode)
    }

    logger.quiet('Version code: ' + versionCode)
    return versionCode
}

/**
 * Returns the short commit hash
 */
def getGitShortHash() {
    def gitHash = '0'

    try {
        gitHash = 'git rev-parse --short HEAD'.execute([], project.rootDir).text.trim()
    } catch (Exception e) {
        logger.error(e.toString() + ': defaulting to dummy build hash ' + gitHash)
    }

    return gitHash
}

/**
 * Returns the current branch name, or prPR_NUMBER if PR_NUMBER is set
 */
def getGitBranch() {
    def branch = 'unk'

    try {
        def prNumber = System.getenv('PR_NUMBER') ?: ''
        if (!prNumber.isEmpty())
            branch = 'pr' + prNumber
        else
            branch = 'git rev-parse --abbrev-ref HEAD'.execute([], project.rootDir).text.trim()
    } catch (Exception e) {
        logger.error(e.toString() + ': defaulting to dummy branch ' + branch)
    }

    return branch
}
