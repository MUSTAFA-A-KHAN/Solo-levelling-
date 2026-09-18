buildscript {
    val compose_version by extra("1.5.4")
    val hilt_version by extra("2.48")
    repositories {
        google()
        maven { url = java.net.URI("https://maven-central.storage-download.googleapis.com/maven2/") }
        mavenCentral()
    }
    dependencies {
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.48")
        classpath("com.android.tools.build:gradle:8.2.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.20")
    }
}
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
}