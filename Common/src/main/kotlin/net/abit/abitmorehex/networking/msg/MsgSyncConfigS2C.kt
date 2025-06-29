package net.abit.abitmorehex.networking.msg

import net.abit.abitmorehex.config.AbitmorehexConfig
import net.minecraft.network.FriendlyByteBuf

data class MsgSyncConfigS2C(val serverConfig: AbitmorehexConfig.ServerConfig) : AbitmorehexMessageS2C {
    companion object : AbitmorehexMessageCompanion<MsgSyncConfigS2C> {
        override val type = MsgSyncConfigS2C::class.java

        override fun decode(buf: FriendlyByteBuf) = MsgSyncConfigS2C(
            serverConfig = AbitmorehexConfig.ServerConfig.decode(buf),
        )

        override fun MsgSyncConfigS2C.encode(buf: FriendlyByteBuf) {
            serverConfig.encode(buf)
        }
    }
}
