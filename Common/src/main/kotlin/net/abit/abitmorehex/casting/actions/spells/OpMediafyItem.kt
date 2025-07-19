package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.abit.abitmorehex.api.ItemTrackerData
import net.abit.abitmorehex.casting.iota.ItemIota
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import java.util.UUID

object OpMediafyItem : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        // 1) Argument #0 must be an EntityIota
        val raw = args.getOrNull(0)
            ?: throw MishapInvalidIota(
                perpetrator   = args[0],              // or some sentinel Iota
                reverseIdx    = 0,                      // “0” means the last argument
                expected      = Component.literal("EntityIota")
            )

        // 2) Check it’s an EntityIota
        val entityIota = raw as? EntityIota
            ?: throw MishapInvalidIota(
                perpetrator   = raw,
                reverseIdx    = 0,
                expected      = Component.literal("EntityIota")
            )

        // 3) Check it wraps an ItemEntity
        val itemEnt = entityIota.entity as? ItemEntity
            ?: throw MishapInvalidIota(
                perpetrator   = raw,
                reverseIdx    = 0,
                expected      = Component.literal("ItemEntity")
            )

        // 3) Copy its ItemStack and assign a new UUID
        val stack = itemEnt.item.copy()
        itemEnt.discard()
        val uuid = UUID.randomUUID()

        // 4) Register it in your public tracker
        val data = ItemTrackerData.get(env.world as ServerLevel)
        data.map[uuid] = stack
        data.setDirty()  // flag for a save

        // 5) Return it as the single Iota on the stack
        return listOf(ItemIota(uuid))
    }
}
