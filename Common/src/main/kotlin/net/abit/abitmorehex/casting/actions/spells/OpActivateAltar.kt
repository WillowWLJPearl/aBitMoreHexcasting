package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import net.abit.abitmorehex.blockentities.RootedTable
import net.abit.abitmorehex.registry.AbitmorehexEffects
import net.abit.abitmorehex.registry.eval.AltarCastingEnvironment
import net.abit.abitmorehex.registry.eval.CircletCastingEnvironment
import net.abit.abitmorehex.registry.item.readIotaList
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

object OpActivateAltar : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val vec3 = (args[0] as Vec3Iota).vec3

        // Calculate media cost: here proportional to area (πr²) or volume (4/3πr³)
        // For simplicity, use area times DUST_UNIT:
        //1/5th dusts per block off the volume
        var iotaItem: ItemStack? = null
        val be = (env.world as ServerLevel).getBlockEntity(BlockPos.containing(vec3.x, vec3.y, vec3.z)) as? RootedTable
        if (be != null) {
            iotaItem = be.getIotaSlot()
        }
        val iota = readIotaList(iotaItem as ItemStack, env.world as ServerLevel)

        val cost: Long = ((iota.size.toDouble()).pow(2)).toLong()

        val particles = listOf(
            ParticleSpray.cloud(vec3, 1.0)
        )

        return SpellAction.Result(Spell(vec3, iota, iotaItem), cost, particles)
    }

    private data class Spell(val pos: Vec3, val iota: List<Iota>, val item: ItemStack) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val level = env.world as ServerLevel
            val image: CastingImage = CastingImage()
            CastingVM(
                image = image,
                env = AltarCastingEnvironment(level, pos,iota, item )
            ).queueExecuteAndWrapIotas(
                iotas = iota,
                world = level
            )
        }
    }
}
