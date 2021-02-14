plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.4.20"
    id("org.jetbrains.dokka") version "1.4.20"
}

group "com.guardian"
version "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    jcenter()
    mavenCentral()
}
dependencies {
    //    dokkaHtmlPlugin "org.jetbrains.dokka:kotlin-as-java-plugin:1.4.20"
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.google.code.gson:gson:2.8.6")
}
