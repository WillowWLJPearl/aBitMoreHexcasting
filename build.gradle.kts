
plugins {
    id("abitmorehex.java")
}


val minecraftVersion: String by project

// scuffed sanity check, because we need minecraftVersion to be in gradle.properties for the hexdoc plugin
libs.versions.minecraft.get().also {
    if (minecraftVersion != it) {
        throw IllegalArgumentException("Mismatched Minecraft version: gradle.properties ($minecraftVersion) != libs.versions.toml ($it)")
    }
}

architectury {
    // this looks up the value from gradle/libs.versions.toml
    minecraft = libs.versions.minecraft.get()
    compileOnly()
}


// 2) Ensure Architectury Loom v1.7.423 (which bundles transformer v5.2.89+)
//    is the only one on the classpath—no version= in plugins { } needed later.
tasks {
    register("runAllDatagen") {
        dependsOn(":Forge:runCommonDatagen")
    }
}


