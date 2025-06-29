@file:JvmName("AbitmorehexAbstractionsImpl")

package net.abit.abitmorehex.fabric

import net.abit.abitmorehex.registry.AbitmorehexRegistrar
import net.minecraft.core.Registry

fun <T : Any> initRegistry(registrar: AbitmorehexRegistrar<T>) {
    val registry = registrar.registry
    registrar.init { id, value -> Registry.register(registry, id, value) }
}
