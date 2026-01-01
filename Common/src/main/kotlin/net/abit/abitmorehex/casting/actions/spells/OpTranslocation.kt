package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import kotlin.math.roundToInt
import kotlin.math.pow

object OpTranslocation : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val origin = (args[0] as Vec3Iota).vec3
        val offset = (args[1] as Vec3Iota).vec3
        val stability = (args.getOrNull(2) as? DoubleIota)?.double?.toInt() ?: 1

        if (stability <= 0) throw MishapBadLocation(origin, "not_valid")

        // Use offset magnitude, and throw if it's TOO FAR (> 32)
        val dist = offset.length()
        if (dist > 32.0) throw MishapBadLocation(origin.add(offset), "too_far")

        val mediaCost = (dist * stability.toDouble().pow(2)).toLong()

        val particleAt = env.castingEntity?.position() ?: origin
        val particles = listOf(ParticleSpray.cloud(particleAt, 0.25))

        return SpellAction.Result(Spell(origin, offset, stability), mediaCost, particles)
    }

    private data class Spell(val origin: Vec3, val offset: Vec3, val stability: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val world = env.world

            val pos1 = BlockPos.containing(origin)
            val jitterScale = (1.0 / stability).coerceAtMost(1.0) * 0.5 // max ±0.5
            val finalOffset = offset.add(
                (Math.random() * 2 - 1) * jitterScale,
                (Math.random() * 2 - 1) * jitterScale,
                (Math.random() * 2 - 1) * jitterScale
            )
            val dx = finalOffset.x.roundToInt()
            val dy = finalOffset.y.roundToInt()
            val dz = finalOffset.z.roundToInt()

            val pos2 = pos1.offset(dx, dy, dz)

            if (pos1 == pos2) return
            if (!world.hasChunkAt(pos1) || !world.hasChunkAt(pos2)) return
            if (!env.canEditBlockAt(pos1) || !env.canEditBlockAt(pos2)) return

            val state1 = world.getBlockState(pos1)
            val state2 = world.getBlockState(pos2)

            // NOTE: This swaps block STATES only. Block entity data needs extra handling (see below).
            world.setBlock(pos1, state2, 3)
            world.setBlock(pos2, state1, 3)
        }
    }
}
