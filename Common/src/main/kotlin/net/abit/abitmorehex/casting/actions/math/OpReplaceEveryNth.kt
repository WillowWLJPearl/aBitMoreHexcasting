package net.abit.abitmorehex.casting.actions.math

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota

object OpReplaceEveryNth : ConstMediaAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc).toMutableList()
        val n = args.getPositiveIntUnderInclusive(1, list.size, argc)
        val replacement = args[2]

        return replaceWithSingleElement(list, n, replacement)
    }

    private fun replaceWithList(list: MutableList<Iota>, n: Int, replacementList: SpellList): List<Iota> {
        val replacements = replacementList.toMutableList()
        var unfilledSpots = 0

        for (i in n - 1 until list.size step n) {
            if (replacements.isNotEmpty()) {
                list[i] = replacements.removeAt(0)
            } else {
                unfilledSpots++
            }
        }

        return if (unfilledSpots > 0) {
            list.asActionResult + listOf(DoubleIota(unfilledSpots.toDouble())).asActionResult
        } else {
            list.asActionResult
        }
    }

    private fun replaceWithSingleElement(list: MutableList<Iota>, n: Int, replacement: Iota): List<Iota> {
        for (i in n - 1 until list.size step n) {
            list[i] = replacement
        }
        return list.asActionResult
    }
}
