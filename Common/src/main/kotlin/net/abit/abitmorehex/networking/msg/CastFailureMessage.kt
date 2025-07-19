// src/commonMain/kotlin/net/abit/abitmorehex/networking/msg/CastFailureMessage.kt
package net.abit.abitmorehex.networking.msg

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.common.lib.HexParticles
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import dev.architectury.networking.NetworkChannel
import dev.architectury.networking.NetworkManager.PacketContext
import me.shedaniel.math.Color
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.Particle
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.sounds.SoundSource
import java.util.*
import java.util.function.Supplier

object CastFailureMessage {
    /** Must match BiConsumer<T, FriendlyByteBuf> */
    private fun encode(msg: CastFailureMessage, buf: FriendlyByteBuf) { }

    /** Must match Function<FriendlyByteBuf, T> */
    private fun decode(buf: FriendlyByteBuf): CastFailureMessage = this

    /**
     * Register as an S2C packet.
     * Handler is a BiConsumer<T, Supplier<PacketContext>>, and Architectury
     * will only invoke it on the client for server→client messages :contentReference[oaicite:1]{index=1}.
     */
    fun register(channel: NetworkChannel) {
        channel.register(
            CastFailureMessage::class.java,
            { _msg, _buf -> /* nothing */ },
            { _buf -> CastFailureMessage },
            { _msg, _ctxSupplier ->
                // client‐only: play sound + particles
                Minecraft.getInstance().execute {
                    val p = Minecraft.getInstance().player ?: return@execute
                    Minecraft.getInstance().level?.playLocalSound(
                        p.x, p.y, p.z,
                        HexSounds.CAST_FAILURE,
                        SoundSource.PLAYERS,
                        1f, 1f, false
                    )
                    ParticleSpray.burst(p.position(), 2.0, 20)
                    val opts: ConjureParticleOptions = ConjureParticleOptions(0xFFFF0000.toInt())
                    repeat(40) { index ->
                        Minecraft.getInstance().level?.addParticle(opts, p.x, p.y, p.z, Random().nextDouble( -0.30,  0.3)
                            , Random().nextDouble( -0.3,  0.3),
                            Random().nextDouble( -0.3,  0.3))
                    }
                }
            }
        )

    }
}
