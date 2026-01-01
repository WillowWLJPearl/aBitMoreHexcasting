package net.abit.abitmorehex.casting.actions.subiota

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.abit.abitmorehex.registry.item.ItemShiftingMedia

object OpGetCurrentSlot : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env !is SubCastingEnvironment)
            throw MishapBadCaster()

        val itemstack = env.getIotaHolder()

        val container = env.getContainer()

        val slotpos = ItemShiftingMedia.findSlotByReference(container, itemstack)



        return listOf(DoubleIota(slotpos.toDouble()))
    }

}