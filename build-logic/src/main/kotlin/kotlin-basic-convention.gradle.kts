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
    implementation(libs.findLibrary("arrow-core").get())

    testImplementation(libs.findLibrary("junit-jupiter-api").get())
    testRuntimeOnly(libs.findLibrary("junit-jupiter-engine").get())

    testImplementation(libs.findLibrary("kotest-runner-junit5").get())
    testImplementation(libs.findLibrary("kotest-assertions-core").get())
    testImplementation(libs.findLibrary("kotest-assertions-arrow").get())


    implementation(project.dependencies.platform(
        libs.findLibrary("koin-bom").get())
    )
    implementation(libs.findLibrary("koin-core").get())
    testImplementation(libs.findLibrary("koin-test").get())
    testImplementation(libs.findLibrary("koin-test-junit5").get())
    testImplementation(libs.findLibrary("kotest-extensions-koin").get())

    implementation(libs.findLibrary("postgres-jdbc-driver").get())
    implementation(libs.findLibrary("h2-jdbc-driver").get())
    implementation(libs.findLibrary("ktorm-core").get())
    implementation(libs.findLibrary("ktorm-support-postgresql").get())
}

tasks.withType<Test>{
    useJUnitPlatform()
}
