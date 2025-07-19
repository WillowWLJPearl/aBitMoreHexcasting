package net.abit.abitmorehex.casting.actions.dicts

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import net.abit.abitmorehex.casting.iota.DictIota

object OpIndex : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val key = (args[0] as Iota)
        val value = (args[1] as Iota)


        val dict = DictIota(mapOf(key to value))
        return listOf(dict)
    }
}