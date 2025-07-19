package net.abit.abitmorehex.casting.actions.dicts

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.abit.abitmorehex.casting.iota.DictIota

object OpEmptyDict : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val emptyDict = DictIota(emptyMap())
        return listOf(emptyDict)
    }
}