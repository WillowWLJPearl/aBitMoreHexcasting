package net.abit.abitmorehex.casting.actions.dicts

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import net.abit.abitmorehex.casting.iota.DictIota
import net.abit.abitmorehex.casting.iota.ItemIota
import net.minecraft.world.entity.player.Player

object OpDictIndex : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val key = (args[0] as Iota)
        val value = (args[1] as Iota)


        if(value is ItemIota || key is ItemIota)
            throw MishapOthersName(env.castingEntity as Player)
        val dict = DictIota(mapOf(key to value))
        return listOf(dict)
    }
}