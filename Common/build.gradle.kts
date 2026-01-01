
plugins {
    id("abitmorehex.minecraft")
    `java-library`
}

architectury {
    common("fabric", "forge")
}

java {
    // Prevent this JAR from being placed on the module path:
    modularity.inferModulePath.set(false)
}
val rei_version: String by project
val emi_version: String by project
dependencies {

    implementation(libs.kotlin.stdlib)
    implementation(kotlin("reflect"))

    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(libs.fabric.loader)
    modApi(libs.architectury)

    modApi(libs.hexcasting.common)

    modApi(libs.clothConfig.common)
    modApi("me.shedaniel:RoughlyEnoughItems-fabric:${rei_version}")
    modApi("dev.emi:emi-xplat-intermediary:${emi_version}:api")

    libs.mixinExtras.common.also {
        implementation(it)
        annotationProcessor(it)
    }
}


tasks.withType<Jar> {
    // Strip out any Automatic-Module-Name so it’s treated as an unnamed module
    manifest {
        attributes.remove("Automatic-Module-Name")
    }
}