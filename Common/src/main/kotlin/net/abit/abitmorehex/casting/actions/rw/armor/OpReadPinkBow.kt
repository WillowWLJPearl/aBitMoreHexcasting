package net.abit.abitmorehex.casting.actions.rw.armor

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity


object OpReadPinkBow : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        // 1) Pull the EntityIota, error if missing or wrong type
        val raw = args.getOrNull(0)
            ?: throw MishapInvalidIota(
                perpetrator = args[0],
                reverseIdx = 0,
                expected = Component.literal("EntityIota")
            )
        val entIota = raw as? EntityIota
            ?: throw MishapInvalidIota(
                perpetrator = raw,
                reverseIdx = 0,
                expected = Component.literal("EntityIota")
            )

        val entity = entIota.entity
        if (entity !is LivingEntity) {
            throw MishapInvalidIota(
                perpetrator = raw,
                reverseIdx = 0,
                expected = Component.literal("LivingEntity")
            )
        }

        // 2) Grab the helmet ItemStack from the entity’s armor slot
        val helmetStack = entity.getItemBySlot(EquipmentSlot.HEAD)

        // 3) Find its Iota‐data holder on that helmet
        val dataHolder = IXplatAbstractions.INSTANCE.findDataHolder(helmetStack)
            ?: throw MishapBadOffhandItem.of(
                helmetStack,
                "hexcasting.mishap.no_iota_on_helmet"
            )

        // 4) Read or fall back to empty Iota
        val datum = dataHolder.readIota(env.world)
            ?: dataHolder.emptyIota()
            ?: throw MishapBadOffhandItem.of(
                helmetStack,
                "hexcasting.mishap.no_iota_on_helmet"
            )

        // 5) Return it
        return listOf(datum)
    }
}
