plugins {
    kotlin("jvm") version "2.4.21"
    id("net.typho.typho_publish") version "1.0.5"
}

group = "net.typho"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://typho.net/maven")
}

dependencies {
    implementation("org.ow2.asm:asm:9.11.1")
    implementation("org.ow2.asm:asm-tree:9.11.1")
    implementation("org.ow2.asm:asm-util:9.11.1")
    implementation("net.typho:parsing_util:2.0.3")
    implementation("net.typho:asm_util:1.4.0")
}

kotlin {
    jvmToolchain(8)
}