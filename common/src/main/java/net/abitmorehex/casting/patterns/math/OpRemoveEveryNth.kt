package net.abitmorehex.casting.patterns.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.casting.iota.Iota

class OpRemoveEveryNth : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val list = args.getList(0, argc)
        val n = args.getPositiveIntUnderInclusive(1, list.size(), argc)

        // Create a new list by filtering out every nth element
        val filteredList = list.filterIndexed { index, _ -> (index + 1) % n != 0 }

        return filteredList.asActionResult
    }
}