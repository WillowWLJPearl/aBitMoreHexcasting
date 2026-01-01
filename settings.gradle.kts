pluginManagement {
    repositories {
        // Repositories where you can get
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://maven.blamejared.com/") }
        maven { url = uri("https://dvs1.progwml6.com/files/maven/") }
        maven { url = uri("https://maven.shedaniel.me/") }
        maven { url = uri("https://modmaven.dev") }
        maven { url = uri("https://maven.terraformersmc.com")}
    }
    plugins {
        // Bind the Loom plugin ID to exactly 1.7.423 here:
        id("dev.architectury.loom")
    }
}

rootProject.name = "AbitMoreHex"
include("Common", "Fabric", "Forge")
