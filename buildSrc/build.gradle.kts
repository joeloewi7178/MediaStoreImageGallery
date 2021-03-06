plugins {
    `kotlin-dsl`
}

repositories {
    google()
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation("com.android.tools.build:gradle:7.0.4")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.40.1")
    // NOTE: Do not place your application dependencies here; they belong
    // in the individual module build.gradle files
}