package net.abit.abitmorehex.networking

import dev.architectury.networking.NetworkChannel
import net.abit.abitmorehex.Abitmorehex
import net.abit.abitmorehex.networking.msg.AbitmorehexMessageCompanion

object AbitmorehexNetworking {
    val CHANNEL: NetworkChannel = NetworkChannel.create(Abitmorehex.id("networking_channel"))

    fun init() {
        for (subclass in AbitmorehexMessageCompanion::class.sealedSubclasses) {
            subclass.objectInstance?.register(CHANNEL)
        }
    }
}
