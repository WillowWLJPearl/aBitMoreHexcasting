package net.abitmorehex.casting.patterns.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import net.minecraft.sound.SoundEvents

class OpCreeperFireworkSpell : SpellAction {
    override val argc = 1
    val cost = 2 * MediaConstants.DUST_UNIT

    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val intensity = (args[0] as DoubleIota).double

        return Triple(
            Spell(intensity),
            cost,
            listOf(ParticleSpray.burst(ctx.caster.pos, intensity))  // Firework particles with intensity
        )
    }

    private data class Spell(val intensity: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val world = ctx.caster.world
            val pos = ctx.caster.pos

            // Play the creeper hissing sound at the caster's location
            world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_CREEPER_PRIMED, ctx.caster.soundCategory, 1.0f, 1.0f)

            // The particle effect is handled by returning ParticleSpray in the execute method.
        }
    }
}
