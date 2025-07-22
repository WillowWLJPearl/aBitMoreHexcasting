package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.mod.HexConfig
import net.abit.abitmorehex.casting.iota.DictIota
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.MinecartItem
import net.minecraft.world.phys.Vec3

object OpThrowItem : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is SubCastingEnvironment)
        {
            val slot = (args[0] as DoubleIota).double.toInt()
            val amount = (args[1] as DoubleIota).double.toInt()
            val inventory = (env.castingEntity as Player).inventory
            val particles = listOf(
                ParticleSpray.cloud((env.castingEntity as Player).position(), 1.0)
            )
            if(inventory.getItem(slot).item == Items.AIR)
                throw MishapBadItem(inventory.getItem(slot).entityRepresentation as ItemEntity, Component.literal("a itemstack"))
            if(inventory.getItem(slot).count < amount)
                throw MishapBadItem(inventory.getItem(slot).entityRepresentation as ItemEntity, Component.literal("a itemstack with a high enough count"))


            return SpellAction.Result(Spell(slot, (env.castingEntity as Player).position(), env.world, inventory, amount), HexConfig.common().shardMediaAmount(), particles)
        }
        else {
        val slot = (args[0] as DoubleIota).double.toInt()
        val amount = (args[1] as DoubleIota).double.toInt()

        val particles = listOf(
            ParticleSpray.cloud(env.getContainerVec3(), 1.0)
        )
        if(env.getContainer().getItem(slot).item == Items.AIR)
        throw MishapBadItem(env.getContainer().getItem(slot).entityRepresentation as ItemEntity, Component.literal("a itemstack"))
        if(env.getContainer().getItem(slot).count < amount)
            throw MishapBadItem(env.getContainer().getItem(slot).entityRepresentation as ItemEntity, Component.literal("a itemstack with a high enough count"))
        return SpellAction.Result(Spell(slot, env.getContainerVec3(), env.world, env.getContainer(), amount), HexConfig.common().shardMediaAmount(), particles)
    }
    }


    private data class Spell(val slot: Int, val vec3: Vec3, val world: ServerLevel, val container: Container, val amount: Int) :
        RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val itementity = ItemEntity(
                world,
                vec3.x, vec3.y, vec3.z,
                container.getItem(slot).copyWithCount(amount)
            ).apply {
                setPickUpDelay(20)
            }
            world.addFreshEntity(itementity)
            container.removeItem(slot, amount)
        }
    }

}