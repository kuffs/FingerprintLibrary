# FingerprintLibrary

Add jitpack in project gradle

    allprojects {
        repositories {
            jcenter()
            mavenCentral()
            maven { url "https://jitpack.io" }
        }
    }

then import library in module gradle.

    implementation 'com.github.kuffs:FingerprintLibrary:0.1.1'
