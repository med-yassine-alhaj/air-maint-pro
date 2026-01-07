plugins {

    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.air_maint_pro"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.air_maint_pro"
        minSdk = 35
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.firebase:firebase-common-ktx:20.4.2")
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    //pour stats
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Pour Bottom Navigation
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Pour CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Pour Fragments
    implementation("androidx.fragment:fragment-ktx:1.6.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    // Pour les QR Codes
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")


// For PDF generation
    implementation("com.itextpdf:itext7-core:7.2.5")
// For file download permissions
    implementation("com.github.yalantis:ucrop:2.2.6")
// For file picker (optional)
    implementation("com.github.jaiselrahman:FilePicker:1.3.2")

    // Pour les dates
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("androidx.core:core-splashscreen:1.0.1")


}