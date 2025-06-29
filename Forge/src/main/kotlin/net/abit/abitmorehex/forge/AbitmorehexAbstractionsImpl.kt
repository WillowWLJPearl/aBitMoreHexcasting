@file:JvmName("AbitmorehexAbstractionsImpl")

package net.abit.abitmorehex.forge

import net.abit.abitmorehex.registry.AbitmorehexRegistrar
import net.minecraftforge.registries.RegisterEvent
import thedarkcolour.kotlinforforge.forge.MOD_BUS

fun <T : Any> initRegistry(registrar: AbitmorehexRegistrar<T>) {
    MOD_BUS.addListener { event: RegisterEvent ->
        event.register(registrar.registryKey) { helper ->
            registrar.init(helper::register)
        }
    }
}
