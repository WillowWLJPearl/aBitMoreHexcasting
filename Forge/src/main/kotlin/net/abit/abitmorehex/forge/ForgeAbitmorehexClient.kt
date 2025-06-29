package net.abit.abitmorehex.forge

import net.abit.abitmorehex.AbitmorehexClient
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import thedarkcolour.kotlinforforge.forge.LOADING_CONTEXT

object ForgeAbitmorehexClient {
    fun init(event: FMLClientSetupEvent) {
        AbitmorehexClient.init()
        LOADING_CONTEXT.registerExtensionPoint(ConfigScreenFactory::class.java) {
            ConfigScreenFactory { _, parent -> AbitmorehexClient.getConfigScreen(parent) }
        }
    }
}
