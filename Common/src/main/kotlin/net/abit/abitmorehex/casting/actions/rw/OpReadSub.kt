package net.abit.abitmorehex.casting.actions.rw

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.abit.abitmorehex.api.SubIotaHolderItem
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack

object OpReadSub : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val player = env.caster ?:
        throw MishapBadOffhandItem.of(null, "iota.read")

        // 1. Build list of (stack, hand) for OFF and MAIN
        val candidates = listOf(
            player.getItemInHand(InteractionHand.OFF_HAND) to InteractionHand.OFF_HAND,
            player.getItemInHand(InteractionHand.MAIN_HAND) to InteractionHand.MAIN_HAND
        )

        // 2. Find the first where the item implements SubIotaHolderItem
        val (handStack, hand) = candidates.firstOrNull { (stack, _) ->
            stack.item is SubIotaHolderItem
        } ?: throw MishapBadOffhandItem.of(null, "iota.read")

        // 3. Cast to SubIotaHolderItem
        val holder = handStack.item as SubIotaHolderItem

        // 4. Read the stored tag or empty value
        val datum = holder.readSubIotaTag(handStack)
            ?.let { IotaType.deserialize(it, env.world) }
            ?: holder.bemptyIota(handStack)
            ?: throw MishapBadOffhandItem.of(handStack, "iota.read")

        return listOf(datum)
    }





}