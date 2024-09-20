buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
        classpath ("com.google.firebase:firebase-crashlytics-gradle:2.9.5")
        classpath("com.android.tools.build:gradle:7.0.4")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.google.firebase.firebase-perf") version "1.4.2" apply false
}