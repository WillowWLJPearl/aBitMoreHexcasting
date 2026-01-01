package net.abit.abitmorehex.casting.actions.rw

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import net.abit.abitmorehex.misc.SubIotaHolderItem
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack

object OpWriteSub : SpellAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        env: CastingEnvironment
    ): SpellAction.Result {
        val iota = args[0]

        val player = env.caster
            ?: throw MishapBadOffhandItem.of(null, "iota.write")

        // 1. Look for a SubIotaHolderItem in OFF or MAIN that *can* write this Iota
        val candidates = listOf(
            player.getItemInHand(InteractionHand.OFF_HAND) to InteractionHand.OFF_HAND,
            player.getItemInHand(InteractionHand.MAIN_HAND) to InteractionHand.MAIN_HAND
        )
        val (handStack, hand) = candidates.firstOrNull { (stack, _) ->
            val holder = stack.item as? SubIotaHolderItem
            holder != null && holder.writeablesub(stack)
                    && holder.canWritesub(stack, iota)
        }
        // 2. If none are writeable, pick any SubIotaHolderItem for error messaging
            ?: candidates.firstOrNull { (stack, _) ->
                stack.item is SubIotaHolderItem
            } ?: throw MishapBadOffhandItem.of(null, "iota.write")

        // 3. Cast to your interface
        val holder = handStack.item as? SubIotaHolderItem
            ?: throw MishapBadOffhandItem.of(handStack, "iota.write")

        // 4. If it truly can't write, error
        if (!holder.canWritesub(handStack, iota))
            throw MishapBadOffhandItem.of(handStack, "iota.readonly", iota.display())


        // 6) Check for “Other’s Name” mishap
        val trueName = MishapOthersName.getTrueNameFromDatum(
            iota, env.castingEntity as? ServerPlayer
        )
        if (trueName != null) {
            throw MishapOthersName(trueName)
        }
        // 5. Schedule the actual write
        return SpellAction.Result(
            Spell(iota, handStack, holder),
            // cost in media:
            0,
            // no extra interactions
            listOf()
        )
    }

    private data class Spell(
        val datum: Iota,
        val stack: ItemStack,
        val holder: SubIotaHolderItem
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // perform the real write (simulate = false)
            holder.writesubDatum(stack, datum)
        }
    }
}
