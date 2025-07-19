package net.abit.abitmorehex.casting.actions.subcast.rw

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapDisallowedSpell
import net.abit.abitmorehex.api.SubIotaHolderItem
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.item.ItemEntity

object OpReadCurrentSub : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env !is SubCastingEnvironment)
            throw MishapBadCaster()

        val itemstack = env.getIotaHolder()
        if(itemstack.item !is SubIotaHolderItem)
      MishapBadItem(itemstack.entityRepresentation as ItemEntity, Component.literal("item with subiota"))

        // 3. Cast to SubIotaHolderItem
        val holder = itemstack.item as SubIotaHolderItem

        // 4. Read the stored tag or empty value
        val datum = holder.readSubIotaTag(itemstack)
            ?.let { IotaType.deserialize(it, env.world) }
            ?: holder.bemptyIota(itemstack)
            ?: throw MishapBadOffhandItem.of(itemstack, "iota.read")

        return listOf(datum)
    }

}