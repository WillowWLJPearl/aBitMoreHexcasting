package net.abit.abitmorehex.casting.actions.dicts

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.abit.abitmorehex.casting.iota.DictIota
import net.minecraft.network.chat.Component

object OpGetIndex : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val dict = (args[0] as DictIota)
        val key = (args[1] as Iota)

        println(dict.serialize())
        val value = dict.get(key)
        println(key.serialize())
        println(value?.serialize())
        if(value === null) throw MishapInvalidIota(key, 0, Component.literal("a valid key for the map"))
        return listOf(value as Iota)
    }
}