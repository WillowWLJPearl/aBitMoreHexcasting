package net.abit.abitmorehex.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import net.abit.abitmorehex.casting.actions.math.OpRemoveEveryNth
import net.abit.abitmorehex.casting.actions.math.OpRandomizeList
import net.abit.abitmorehex.casting.actions.math.OpReplaceEveryNth
import net.abit.abitmorehex.casting.actions.spells.OpWillowsNurturingWhispers

object AbitmorehexActions : AbitmorehexRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {
    val WILLOWSNURTURINGWHISPERS = make("willowsnurturingwhispers", HexDir.NORTH_EAST, "wqaqwdqawaaqed", OpWillowsNurturingWhispers)
    val THOUGHTCLEAR = make("thoughtclear", HexDir.NORTH_WEST, "adadadeaqqq", OpRemoveEveryNth)
    val THOUGHTCLUTTER = make("thoughtclutter", HexDir.NORTH_EAST, "dadadawedqdew", OpReplaceEveryNth)
    val RANDOMIZELIST = make("randomizelist", HexDir.NORTH_WEST, "adwqqqqae", OpRandomizeList)

    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }

    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
