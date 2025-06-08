plugins {
    id("kotlin-basic-convention")
    // Apply the application plugin to add support for building a CLI application in Java.
    application
}

application {
    // Define the main class for the application.
    mainClass = "org.ama.delivery.app.AppKt"
}

dependencies{
    implementation(project(":api"))
}