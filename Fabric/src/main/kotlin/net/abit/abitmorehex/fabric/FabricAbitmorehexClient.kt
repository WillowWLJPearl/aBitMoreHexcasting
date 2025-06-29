package net.abit.abitmorehex.fabric

import net.abit.abitmorehex.AbitmorehexClient
import net.fabricmc.api.ClientModInitializer

object FabricAbitmorehexClient : ClientModInitializer {
    override fun onInitializeClient() {
        AbitmorehexClient.init()
    }
}
