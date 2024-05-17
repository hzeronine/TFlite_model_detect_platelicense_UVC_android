plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs")
}

android {
    namespace = "com.example.platedetect2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.platedetect"
        minSdk = 29
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    dataBinding {
        enable = true
    }
    androidResources {
        noCompress.add("tflite")
    }
    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //
    // Kotlin lang
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    // App compat and UI things
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")

    // Navigation library
    val nav_version = "2.7.0"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // CameraX core library
    val camerax_version = "1.3.0-alpha04"
    implementation("androidx.camera:camera-core:$camerax_version")

    // CameraX Camera2 extensions
    implementation("androidx.camera:camera-camera2:$camerax_version")

    // CameraX Lifecycle library
    implementation("androidx.camera:camera-lifecycle:$camerax_version")

    // CameraX View class
    implementation("androidx.camera:camera-view:$camerax_version")

    // WindowManager
    implementation("androidx.window:window:1.2.0")

    // Unit testing
//    testImplementation("androidx.test.ext:junit:1.1.3")
//    testImplementation("androidx.test:rules:1.4.0")
//    testImplementation("androidx.test:runner:1.4.0")
//    testImplementation("androidx.test.espresso:espresso-core:3.4.0")
//    testImplementation("org.robolectric:robolectric:4.4")

    // Instrumented testing
//    androidTestImplementation("androidx.test.ext:junit:1.1.3")
//    androidTestImplementation("androidx.test:core:1.4.0")
//    androidTestImplementation("androidx.test:rules:1.4.0")
//    androidTestImplementation("androidx.test:runner:1.4.0")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("org.tensorflow:tensorflow-lite-task-vision:0.4.4")
    // Import the GPU delegate plugin Library for GPU inference
    implementation("org.tensorflow:tensorflow-lite-gpu-delegate-plugin:0.4.4")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.15.0")


    //import UVC camera
//    val fileTreeResult = fileTree("libs") {
//        include("*.jar")
//    }
//    implementation(fileTreeResult)
//    implementation("com.serenegiant:common:8.9.5")
//    implementation(files("libs/libusbcamera-v4.1.1.aar"))
//    implementation(files("libs/libusbcommon-v4.1.1.aar"))
//    implementation(files("libs/common-8.9.5.aar"))

//    implementation("com.gitee.yunianvh:rtsp-rtmp-stream-demo:usb-stream-1.3.21")
//    implementation("com.herohan:UVCAndroid:1.0.5")
    implementation("com.github.getActivity:XXPermissions:13.5")
    implementation(project(":usbSerialForAndroid"))
    implementation(project(":libuvccamera"))



    //////////////////////////

    implementation ("com.google.android.gms:play-services-mlkit-barcode-scanning:16.1.2")


    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.gms:play-services-auth:21.0.0")


    implementation("androidx.camera:camera-view:1.3.3")
    implementation("androidx.camera:camera-lifecycle:1.3.3")
    implementation ("androidx.camera:camera-camera2:1.3.3")
    implementation("com.google.mlkit:vision-common:17.3.0")
    implementation("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.0")


}