val kotlinVersion = "2.0.0" // Проверить, должен совпадать с версией в libs.versions.toml

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    // Явно указываем, какую версию Kotlin использовать для компиляции
    // самих плагинов. Это гарантирует консистентность с основным проектом.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
}