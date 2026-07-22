package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.common.items.storage.ItemFocus
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.abit.abitmorehex.misc.ItemTrackerData
import net.abit.abitmorehex.casting.iota.ItemIota
import net.abit.abitmorehex.registry.AbitmoreItems
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.abit.abitmorehex.registry.item.Thought
import net.fabricmc.loader.impl.lib.sat4j.core.Vec
import net.minecraft.core.Position
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3

object OpMaterializeThought : SpellAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {

            val particles = listOf(
                ParticleSpray.cloud(env.castingEntity?.position() as Vec3, 1.0)
            )

            return SpellAction.Result(Spell(env.world, env.caster as Player, env.castingHand), HexConfig.common().shardMediaAmount(), particles)
    }
    fun getStaffcastVm(player: ServerPlayer, hand: InteractionHand) =
        IXplatAbstractions.INSTANCE.getStaffcastVM(player, hand)
    private data class Spell(val world: ServerLevel, val player: Player, val hand : InteractionHand) :
        RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val thoughtStack = ItemStack(AbitmoreItems.THOUGHT.value, 1)

            val stack : MutableList<Iota> = getStaffcastVm(player as ServerPlayer, hand).image.stack as MutableList<Iota>
            val listiota: ListIota = ListIota(stack)
            val tag = thoughtStack.orCreateTag
            tag.put(ItemFocus.TAG_DATA, IotaType.serialize(listiota))


            player.server.execute {
                IXplatAbstractions.INSTANCE.clearCastingData(player)
                player.closeContainer()
            }

            if(player.inventory.add(thoughtStack))
            {
                player.inventory.setItem(player.inventory.freeSlot, thoughtStack)
            }
            else
            {
                world.addFreshEntity(ItemEntity(world, player.x, player.y, player.z, thoughtStack))
            }


        }
    }
}
