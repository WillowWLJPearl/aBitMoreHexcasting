package net.abitmorehex.casting.patterns.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.sound.SoundEvents
import net.minecraft.util.math.Vec3d

class OpCreeperFireworkSpell : SpellAction {
    override val argc = 1
    // Make sure it's a Long for the cost in SpellAction.Result
    private val cost: Long = 2L * MediaConstants.DUST_UNIT

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): SpellAction.Result {
        val entity = ctx.castingEntity
        val intensity = (args[0] as DoubleIota).double
        // If entity.pos doesn't exist, create a Vec3d:
        val position = entity?.let { Vec3d(it.x, entity.y, entity.z) }

        // Return the triple (RenderedSpell, Long, List<ParticleSpray>)
        return SpellAction.Result(
            Spell(intensity),
            cost,
            listOf(position?.let { ParticleSpray.burst(it, intensity) }) as List<ParticleSpray>
        )
    }

    private data class Spell(val intensity: Double) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            val world = ctx.world
            val entity = ctx.castingEntity ?: return

            world.playSound(
                null,
                entity.x, entity.y, entity.z,
                SoundEvents.ENTITY_CREEPER_PRIMED,
                entity.soundCategory,  // if soundCategory doesn't exist, do entity.getSoundCategory()
                intensity.toFloat(),
                1.0f
            )
        }
    }
}
