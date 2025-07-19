package net.abit.abitmorehex.casting.actions.subcast.rw

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapDisallowedSpell
import at.petrak.hexcasting.api.mod.HexConfig
import net.abit.abitmorehex.api.SubIotaHolderItem
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack

object OpWriteCurrentSub : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is SubCastingEnvironment)
            throw MishapBadCaster()
        val iota = (args[0] as Iota)

        val itemstack = env.getIotaHolder()
        if(itemstack.item !is SubIotaHolderItem)
            MishapBadItem(itemstack.entityRepresentation as ItemEntity, Component.literal("item with subiota"))

        // 3. Cast to SubIotaHolderItem
        val holder = itemstack.item as SubIotaHolderItem

        val particles = listOf(
            ParticleSpray.cloud(env.getContainerVec3(), 1.0)
        )

        return SpellAction.Result(Spell(itemstack, iota, holder), HexConfig.common().shardMediaAmount(), particles )
    }
    private data class Spell(
        val stack : ItemStack,
        val datum: Iota,
        val holder: SubIotaHolderItem
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // Actually write into the helmet (simulate=false)
            holder.writesubDatum(stack, datum)
        }
    }

}