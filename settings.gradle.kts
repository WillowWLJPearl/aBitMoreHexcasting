pluginManagement {
    repositories {
        // Repositories where you can get
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://maven.blamejared.com/") }
    }
    plugins {
        // Bind the Loom plugin ID to exactly 1.7.423 here:
        id("dev.architectury.loom")
    }
}

rootProject.name = "AbitMoreHex"
include("Common", "Fabric", "Forge")
