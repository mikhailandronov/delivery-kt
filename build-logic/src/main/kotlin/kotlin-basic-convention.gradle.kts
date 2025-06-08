import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

// Эта строка применит плагин kotlin("jvm") к любому подпроекту,
// использующему `id("kotlin-basic-convention")`
plugins {
    kotlin("jvm")
}

// Эти строки настроят jvmToolchain для любого подпроекта,
// использующего `id("kotlin-basic-convention")`
extensions.configure<KotlinJvmProjectExtension> {
    jvmToolchain(21)
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    // implementation(libs.findLibrary("kotlin-stdlib").get())
    testImplementation(libs.findLibrary("junit-jupiter-api").get())
    testImplementation(libs.findLibrary("kotlin-test-junit5").get())
    testRuntimeOnly(libs.findLibrary("junit-jupiter-engine").get())
}

tasks.withType<Test>{
    useJUnitPlatform()
}
