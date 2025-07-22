package net.abit.abitmorehex.casting.actions.dicts

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import net.abit.abitmorehex.casting.iota.DictIota
import net.abit.abitmorehex.casting.iota.ItemIota
import net.minecraft.world.entity.player.Player

object OpDictAppendDict : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val dict1 = (args[0] as DictIota)
        val dict2 = (args[1] as DictIota)

        // expose the old map, merge in the new pair, and build a brand‑new DictIota
        val newEntries = dict1.asMap() + dict2.asMap()
        val newdict = DictIota(newEntries)

        return listOf(newdict)
    }
}
