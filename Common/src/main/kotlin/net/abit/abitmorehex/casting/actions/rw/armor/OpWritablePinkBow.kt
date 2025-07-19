package net.abit.abitmorehex.casting.actions.rw.armor

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import java.util.UUID

object OpWritablePinkBow : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        // 1) Must get an EntityIota
        val raw = args.getOrNull(0)
            ?: throw MishapInvalidIota(
                perpetrator   = args[0],
                reverseIdx    = 0,
                expected      = Component.literal("EntityIota")
            )
        val entIota = raw as? EntityIota
            ?: throw MishapInvalidIota(
                perpetrator   = raw,
                reverseIdx    = 0,
                expected      = Component.literal("EntityIota")
            )

        val living = entIota.entity as? LivingEntity
            ?: throw MishapInvalidIota(raw, 1, Component.literal("LivingEntity"))

        val helmetStack  = living.getItemBySlot(EquipmentSlot.HEAD)

        // 3) Look up its Iota holder
        val holder = IXplatAbstractions.INSTANCE.findDataHolder(helmetStack)
            ?: return listOf(BooleanIota(false))

        // 4) Ask if it's writeable
        val canWrite = holder.writeable()

        // 5) Return that as a BooleanIota
         return listOf(BooleanIota(canWrite))
    }
}
