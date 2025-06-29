@file:JvmName("AbitmorehexAbstractions")

package net.abit.abitmorehex

import dev.architectury.injectables.annotations.ExpectPlatform
import net.abit.abitmorehex.registry.AbitmorehexRegistrar

fun initRegistries(vararg registries: AbitmorehexRegistrar<*>) {
    for (registry in registries) {
        initRegistry(registry)
    }
}

@ExpectPlatform
fun <T : Any> initRegistry(registrar: AbitmorehexRegistrar<T>) {
    throw AssertionError()
}
