plugins {
    kotlin("jvm") version "2.4.20"
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass.set("cinema.MainKt")
}

kotlin {
    jvmToolchain(25)
}
