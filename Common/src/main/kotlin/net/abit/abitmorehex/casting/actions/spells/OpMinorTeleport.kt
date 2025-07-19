package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import net.abit.abitmorehex.registry.AbitmorehexEffects
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3
import kotlin.math.pow
import kotlin.reflect.jvm.internal.impl.serialization.deserialization.descriptors.DeserializedContainerAbiStability

object OpMinorTeleport : SpellAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val entity = (args[0] as EntityIota).entity
        val offset = (args[1] as Vec3Iota).vec3
        val stability = (args.getOrNull(2) as? DoubleIota)
            ?.double
            ?.toInt()
            ?: 1

        if(offset.distanceTo(entity.position()) < 16) throw MishapBadLocation(entity.position().add(offset), "too_far")
        if(stability <= 0) throw MishapBadLocation(offset.scale(stability.toDouble()), "not_valid")
        // Calculate media cost: here proportional to area (πr²) or volume (4/3πr³)
        // For simplicity, use area times DUST_UNIT:
        val cost: Long = (offset.distanceTo(entity.position()) * stability.toDouble().pow(2)).toLong()
        //1/5th dusts per block off the volume
        val particles = listOf(
            ParticleSpray.cloud(entity.position(), cost.toDouble())
        )

        return SpellAction.Result(Spell(entity, offset, stability), cost, particles)
    }

    private data class Spell(val entity: net.minecraft.world.entity.Entity, val offset: Vec3, val stability: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {

            var newx = (offset.x+0.1) * ((-20/stability..20/stability).random())/10
            var newy =(offset.y+0.1) * ((-20/stability..20/stability).random())/10
            var newz =(offset.z+0.1) * ((-20/stability..20/stability).random())/10
            entity.teleportRelative(newx, newy, newz)


        }
    }
}
