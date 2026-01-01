package net.abit.abitmorehex.networking.msg

import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import dev.architectury.networking.NetworkChannel
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import kotlin.math.pow

object SoulJarParticles {
    data class Msg(val pos: BlockPos, val count: Int)

    fun register(channel: NetworkChannel) {
        println("[SoulJarParticles] register()")
        channel.register(
            Msg::class.java,
            { msg, buf -> encode(msg, buf) },
            { buf -> decode(buf) },
            { msg, _ ->
                // your style: jump to client thread directly
                Minecraft.getInstance().execute {
                    handleClient(msg)
                }
            }
        )
    }

    private fun encode(msg: Msg, buf: FriendlyByteBuf) {
        buf.writeBlockPos(msg.pos)
        buf.writeVarInt(msg.count)
    }
    private fun decode(buf: FriendlyByteBuf): Msg =
        Msg(buf.readBlockPos(), buf.readVarInt())

    private fun handleClient(msg: Msg) {
        val mc = Minecraft.getInstance()
        val level = mc.level ?: return
        val r = level.random

        // debug
        println("[SoulJarParticles] client recv pos=${msg.pos} count=${msg.count}")

        // Jar inner cavity (block-local 0..1)
        val minX = 4.1 / 16.0; val maxX = 11.9 / 16.0
        val minY = 1.2 / 16.0; val maxY = 10.0 / 16.0
        val minZ = 4.1 / 16.0; val maxZ = 11.9 / 16.0

        repeat(msg.count.coerceAtLeast(1)) {
            val x = msg.pos.x + Mth.lerp(r.nextDouble(), minX, maxX)
            val y = msg.pos.y + Mth.lerp(r.nextDouble(), minY, maxY)
            val z = msg.pos.z + Mth.lerp(r.nextDouble(), minZ, maxZ)

            // dark→light blue gradient, full alpha (RRGGBBAA)
            val color = lerpRgba(0x1030A0FF.toInt(), 0x88C7FFFF.toInt(), r.nextFloat())

            level.addParticle(
                ConjureParticleOptions(color),
                x, y, z,
                r.nextGaussian() * 0.006, 0.008 + r.nextDouble() * 0.004, r.nextGaussian() * 0.006
            )
        }
    }

    private fun lerpRgba(a: Int, b: Int, t: Float): Int {
        fun ch(v: Int, s: Int) = (v ushr s) and 0xFF
        val ar = ch(a,24); val ag = ch(a,16); val ab = ch(a,8); val aa = ch(a,0)
        val br = ch(b,24); val bg = ch(b,16); val bb = ch(b,8); val ba = ch(b,0)
        fun L(x: Int, y: Int) = (x + ((y - x) * t)).toInt().coerceIn(0,255)
        return (L(ar,br) shl 24) or (L(ag,bg) shl 16) or (L(ab,bb) shl 8) or L(aa,ba)
    }

    /** Server helper: send to players in this dimension within range (only `sendToPlayers` available) */
    fun send(level: ServerLevel, pos: BlockPos, count: Int, range: Double = 32.0) {
        if (count <= 0) return
        val cx = pos.x + 0.5; val cy = pos.y + 0.5; val cz = pos.z + 0.5
        val r2 = range.pow(2)
        val recipients = level.players()
            .filterIsInstance<ServerPlayer>()
            .filter { (it.x - cx).pow(2) + (it.y - cy).pow(2) + (it.z - cz).pow(2) <= r2 }

        println("[SoulJarParticles] send -> ${recipients.size} players @ $pos (count=$count)")
        if (recipients.isNotEmpty()) {
            net.abit.abitmorehex.networking.AbitmorehexNetworking.CHANNEL
                .sendToPlayers(recipients, Msg(pos, count))
        }
    }
}
