// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
val java_version = JavaVersion.VERSION_1_10
buildscript {
    val kotlin_version by extra("1.9.0")
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://maven.aliyun.com/repository/public/") }
        maven { url = uri("https://maven.aliyun.com/repository/google/") }
        maven { url = uri("https://maven.aliyun.com/repository/jcenter/") }
        maven { url = uri("https://maven.aliyun.com/repository/central/")}
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.0")
        classpath("de.undercouch:gradle-download-task:5.6.0")
    }
    extra.apply {
        set("versionCompiler", "33")
        set("minSdkVersion", "29")
        set("versionTarget", "33")
        set("javaSourceCompatibility", "${JavaVersion.VERSION_1_8}")
    }
}