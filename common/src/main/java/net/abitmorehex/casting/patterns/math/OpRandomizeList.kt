package net.abitmorehex.casting.patterns.math

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.*
import kotlin.random.Random

class OpRandomizeList : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        // The first argument is the list to randomize
        val list = (args[0] as ListIota).list
        var limit = list.size()  // Default limit is the entire list

        // If a second argument (limit) is provided, use it
        if (args.size > 1 && args[1] is DoubleIota) {
            limit = (args[1] as DoubleIota).double.toInt().coerceAtMost(list.size())
        }

        // Shuffle the list and take only the required limit
        val shuffledList = list.shuffled(Random).take(limit)

        return listOf(ListIota(shuffledList))
    }
}
