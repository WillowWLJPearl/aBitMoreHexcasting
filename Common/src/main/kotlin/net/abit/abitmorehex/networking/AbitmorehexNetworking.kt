package net.abit.abitmorehex.networking

import dev.architectury.networking.NetworkChannel
import net.abit.abitmorehex.Abitmorehex
import net.abit.abitmorehex.networking.msg.AbitmorehexMessageCompanion
import net.abit.abitmorehex.networking.msg.MsgSyncConfigS2C
import net.abit.abitmorehex.networking.msg.MsgSyncConfigS2C.Companion.encode

object AbitmorehexNetworking {
    val CHANNEL: NetworkChannel = NetworkChannel.create(Abitmorehex.id("networking_channel"))

    fun init() {
        CHANNEL.register(
            MsgSyncConfigS2C::class.java,
            { msg, buf -> msg.encode(buf) },
            MsgSyncConfigS2C.Companion::decode,
            MsgSyncConfigS2C.Companion::apply
        )

        // You can do the same for CastFailure, CastSuccess, SoulJarParticles.Msg, etc.
    }

}
