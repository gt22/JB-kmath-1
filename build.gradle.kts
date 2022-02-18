import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.10"
}

group = "com.frgm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.kotlin.link")
}

dependencies {
    api("space.kscience:kmath-core:0.3.0-dev-14")
    api("space.kscience:kmath-stat:0.3.0-dev-14")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}