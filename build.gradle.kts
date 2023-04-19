plugins {
    kotlin("jvm") version "1.8.0"
    id("org.jetbrains.compose") version "1.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0" apply false
    id("org.jetbrains.changelog") version "1.3.0"
    id("com.bybutter.sisyphus.project") version "1.3.33"
    `java-library`
}

group = "com.bybutter.compose"
version = "1.0.2"
description = "JetBrains UI Kit for Compose Desktop"

repositories {
    mavenLocal()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(kotlin("stdlib"))

    // FIXME this is a temporary workaround
    implementation(compose.desktop.macos_arm64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.macos_x64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.linux_x64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.linux_arm64) {
        exclude("org.jetbrains.compose.material")
    }
    implementation(compose.desktop.windows_x64) {
        exclude("org.jetbrains.compose.material")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<JavaCompile>().configureEach {
    targetCompatibility = "11"
    sourceCompatibility = "11"
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "11"
    kotlinOptions.freeCompilerArgs += listOf(
        "-P",
        "plugin:androidx.compose.compiler.plugins.kotlin:suppressKotlinVersionCompatibilityCheck=true"
    )
}
