package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import net.abit.abitmorehex.registry.AbitmorehexEffects
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import kotlin.math.pow

object OpEvokeLeechingVines : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val entity = (args[0] as EntityIota).entity
        val duration = (args[1] as DoubleIota).double.toInt()
        val amplifier = (args.getOrNull(2) as? DoubleIota)
            ?.double
            ?.toInt()
            ?: 1

        // Calculate media cost: here proportional to area (πr²) or volume (4/3πr³)
        // For simplicity, use area times DUST_UNIT:
        val cost: Long = ((duration.toDouble()* 1.2 * amplifier).pow(2)).toLong()
            //1/5th dusts per block off the volume
        val particles = listOf(
            ParticleSpray.cloud(entity.position(), amplifier.toDouble())
        )

        return SpellAction.Result(Spell(entity, duration, amplifier), cost, particles)
    }

    private data class Spell(val entity: net.minecraft.world.entity.Entity, val duration: Int, val amplifier: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (entity is LivingEntity) {
                entity as LivingEntity
                val instance = MobEffectInstance(AbitmorehexEffects.LEECHING_VINES.value, duration*20, amplifier-1)
                entity.addEffect(instance)

            }
        }
    }
}
