pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://typho.net/maven")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "compilation_util"