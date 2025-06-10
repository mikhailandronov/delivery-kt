val kotlinVersion = "2.1.21" // Проверить, должен совпадать с версией в libs.versions.toml

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}