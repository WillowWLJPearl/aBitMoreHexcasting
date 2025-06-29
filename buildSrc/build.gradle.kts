import dev.panuszewski.gradle.pluginMarker

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven { url = uri("https://maven.architectury.dev/") }
    maven { url = uri("https://maven.fabricmc.net/") }
    maven { url = uri("https://maven.minecraftforge.net/") }
    maven { url = uri("https://maven.blamejared.com/") }
}

dependencies {
    // plugins used in convention plugins
    implementation(pluginMarker(buildLibs.plugins.kotlin.jvm))
    implementation(pluginMarker(buildLibs.plugins.architectury.asProvider()))
    implementation(pluginMarker(buildLibs.plugins.architectury.loom))
    implementation(pluginMarker(buildLibs.plugins.shadow))
    implementation(pluginMarker(buildLibs.plugins.modPublish))
    implementation(pluginMarker(buildLibs.plugins.pkJson5))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(buildLibs.versions.java.get())
    }
}
