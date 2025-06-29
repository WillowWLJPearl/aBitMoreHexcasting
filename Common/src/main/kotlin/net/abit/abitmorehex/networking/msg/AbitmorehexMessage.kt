package net.abit.abitmorehex.networking.msg

import dev.architectury.networking.NetworkChannel
import dev.architectury.networking.NetworkManager.PacketContext
import net.abit.abitmorehex.Abitmorehex
import net.abit.abitmorehex.networking.AbitmorehexNetworking
import net.abit.abitmorehex.networking.handler.applyOnClient
import net.abit.abitmorehex.networking.handler.applyOnServer
import net.fabricmc.api.EnvType
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerPlayer
import java.util.function.Supplier

sealed interface AbitmorehexMessage

sealed interface AbitmorehexMessageC2S : AbitmorehexMessage {
    fun sendToServer() {
        AbitmorehexNetworking.CHANNEL.sendToServer(this)
    }
}

sealed interface AbitmorehexMessageS2C : AbitmorehexMessage {
    fun sendToPlayer(player: ServerPlayer) {
        AbitmorehexNetworking.CHANNEL.sendToPlayer(player, this)
    }

    fun sendToPlayers(players: Iterable<ServerPlayer>) {
        AbitmorehexNetworking.CHANNEL.sendToPlayers(players, this)
    }
}

sealed interface AbitmorehexMessageCompanion<T : AbitmorehexMessage> {
    val type: Class<T>

    fun decode(buf: FriendlyByteBuf): T

    fun T.encode(buf: FriendlyByteBuf)

    fun apply(msg: T, supplier: Supplier<PacketContext>) {
        val ctx = supplier.get()
        when (ctx.env) {
            EnvType.SERVER, null -> {
                Abitmorehex.LOGGER.debug("Server received packet from {}: {}", ctx.player.name.string, this)
                when (msg) {
                    is AbitmorehexMessageC2S -> msg.applyOnServer(ctx)
                    else -> Abitmorehex.LOGGER.warn("Message not handled on server: {}", msg::class)
                }
            }
            EnvType.CLIENT -> {
                Abitmorehex.LOGGER.debug("Client received packet: {}", this)
                when (msg) {
                    is AbitmorehexMessageS2C -> msg.applyOnClient(ctx)
                    else -> Abitmorehex.LOGGER.warn("Message not handled on client: {}", msg::class)
                }
            }
        }
    }

    fun register(channel: NetworkChannel) {
        channel.register(type, { msg, buf -> msg.encode(buf) }, ::decode, ::apply)
    }
}
