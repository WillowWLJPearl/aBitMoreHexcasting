package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.abit.abitmorehex.api.ItemTrackerData
import net.abit.abitmorehex.casting.iota.ItemIota
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec3

object OpWeaveItem : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        // 1) Extract spawn position
        val rawPos = args.getOrNull(0)
            ?: throw MishapInvalidIota(
                perpetrator = args[0],
                reverseIdx = 1,
                expected = Component.literal("Vec3Iota")
            )
        val posIota = rawPos as? Vec3Iota
            ?: throw MishapInvalidIota(
                perpetrator = rawPos,
                reverseIdx = 1,
                expected = Component.literal("Vec3Iota")
            )

        // 2) Extract the ItemIota
        val rawItem = args.getOrNull(1)
            ?: throw MishapInvalidIota(
                perpetrator = args[1],
                reverseIdx = 0,
                expected = Component.literal("ItemIota")
            )
        val itemIota = rawItem as? ItemIota
            ?: throw MishapInvalidIota(
                perpetrator = rawItem,
                reverseIdx = 0,
                expected = Component.literal("ItemIota")
            )

        // 3) Remove the stack from the tracker
        val data = ItemTrackerData.get(env.world as ServerLevel)
        val stack = data.map.remove(itemIota.id) ?: throw MishapInvalidIota(
            perpetrator = rawItem,
            reverseIdx = 0,
            expected = Component.literal("existing ItemIota UUID")
        )

        data.setDirty()

        // 4) Spawn the ItemEntity on the server
        if (!env.world.isClientSide) {
            val itemEnt = ItemEntity(env.world, posIota.vec3.x(), posIota.vec3.y(), posIota.vec3.z(), stack)
            env.world.addFreshEntity(itemEnt)
            // 5) Return the new entity as an EntityIota
            return listOf(EntityIota(itemEnt))
        }

        // client side, just return nothing
        return emptyList()
    }
}
