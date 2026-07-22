package net.abit.abitmorehex.casting.actions.dicts

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import net.abit.abitmorehex.casting.iota.DictIota
import net.abit.abitmorehex.casting.iota.ItemIota
import net.minecraft.world.entity.player.Player

object OpDictAppend : ConstMediaAction {
    override val argc = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val dict  = args[0] as DictIota
        val key   = args[1]
        val value = args[2]

        if (key is ItemIota || value is ItemIota)
            throw MishapOthersName(env.castingEntity as Player)

        // expose the old map, merge in the new pair, and build a brand‑new DictIota
        var dictcopy = dict.asMap()
        if(dictcopy.containsKey(key)) {
            dictcopy= dict.asMap().minus(key)
        }
        val newEntries = dictcopy + (key to value)
        val newdict = DictIota(newEntries)

        return listOf(newdict)
    }
}
