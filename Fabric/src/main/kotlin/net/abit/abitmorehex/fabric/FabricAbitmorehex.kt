package net.abit.abitmorehex.fabric

import net.abit.abitmorehex.Abitmorehex
import net.fabricmc.api.ModInitializer

object FabricAbitmorehex : ModInitializer {
    override fun onInitialize() {
        Abitmorehex.init()
    }
}
