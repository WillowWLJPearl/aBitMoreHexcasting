package net.abit.abitmorehex.networking.handler

import dev.architectury.networking.NetworkManager.PacketContext
import net.abit.abitmorehex.config.AbitmorehexConfig
import net.abit.abitmorehex.networking.msg.*

fun AbitmorehexMessageS2C.applyOnClient(ctx: PacketContext) = ctx.queue {
    when (this) {
        is MsgSyncConfigS2C -> {
            AbitmorehexConfig.onSyncConfig(serverConfig)
        }

        // add more client-side message handlers here
    }
}
