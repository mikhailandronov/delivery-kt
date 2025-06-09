import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

// Применение kotlin("jvm") к любому подпроекту, использующему `id("kotlin-basic-convention")`
plugins {
    kotlin("jvm")
}

// Настройка jvmToolchain для любого подпроекта, использующего `id("kotlin-basic-convention")`
extensions.configure<KotlinJvmProjectExtension> {
    jvmToolchain(21)
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    testImplementation(libs.findLibrary("junit-jupiter-api").get())
    testRuntimeOnly(libs.findLibrary("junit-jupiter-engine").get())

    // Runner, который интегрируется с JUnit 5 Platform
    testImplementation(libs.findLibrary("kotest-runner-junit5").get())
    // Библиотека для написания ассертов (shouldBe, shouldThrow и т.д.)
    testImplementation(libs.findLibrary("kotest-assertions-core").get())
    // Опционально: для property-based testing
    testImplementation(libs.findLibrary("kotest-property").get())
}

tasks.withType<Test>{
    useJUnitPlatform()
}
