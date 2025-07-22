package net.abit.abitmorehex.casting.actions.dicts

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import net.abit.abitmorehex.casting.iota.DictIota
import net.minecraft.network.chat.Component

object OpGetKeys : ConstMediaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val dict = (args[0] as DictIota)


        val keys = dict.asMap().keys
        if(keys === emptySet<Iota>()) throw MishapInvalidIota(keys.toList() as ListIota, 0, Component.literal("keys in the map"))
        return keys.toList().asActionResult
    }
}