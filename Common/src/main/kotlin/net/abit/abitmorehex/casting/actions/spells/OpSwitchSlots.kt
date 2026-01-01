package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadLocation
import at.petrak.hexcasting.api.casting.mishaps.MishapEvalTooMuch
import at.petrak.hexcasting.api.mod.HexConfig
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.abit.abitmorehex.registry.eval.mishaps.MishapWrongSlot
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

object OpSwitchSlots : SpellAction{
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val slot1 = (args[0] as DoubleIota).double
        val slot2 = (args[1] as DoubleIota).double
        var particles: List<ParticleSpray>
        var container: Container
        var level: ServerLevel
        if (env is SubCastingEnvironment)
        {
             particles = listOf(
                ParticleSpray.cloud(env.getContainerVec3(), 1.0)
            )
            container = env.getContainer()
            level = env.world
        }
        else {
             particles = listOf(
                ParticleSpray.cloud(env.castingEntity?.position() as Vec3, 1.0)
            )
            val player = env.castingEntity as Player
            container = player.inventory
            level = env.world
        }
        if((
                    container.containerSize > slot1 || container.containerSize > slot2)
            &&
            (slot1 >= 0 || slot2 >= 0)
            ) {
            return SpellAction.Result(Spell(slot1.toInt(), slot2.toInt(), level, container), HexConfig.common().dustMediaAmount()/10, particles)

        } else throw MishapWrongSlot(slot1.toInt(), listOf(0, container.containerSize))

         }
    private data class Spell(val slot1: Int, val slot2: Int, val world: ServerLevel, val container: Container) :
        RenderedSpell {
        override fun cast(env: CastingEnvironment) {
           val item1 = container.getItem(slot1)
            val item2 = container.getItem(slot2)
            container.setItem(slot2, item1)
            container.setItem(slot1, item2)
        }
        }
}