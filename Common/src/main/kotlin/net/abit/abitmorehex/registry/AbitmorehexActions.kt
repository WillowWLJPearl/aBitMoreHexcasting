package net.abit.abitmorehex.registry

import at.petrak.hexcasting.api.casting.ActionRegistryEntry
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.math.HexDir
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexActions
import net.abit.abitmorehex.casting.actions.altars.OpContainerPos
import net.abit.abitmorehex.casting.actions.altars.OpInfusionCrafting
import net.abit.abitmorehex.casting.actions.dicts.OpEmptyDict
import net.abit.abitmorehex.casting.actions.dicts.OpGetIndex
import net.abit.abitmorehex.casting.actions.dicts.OpIndex
import net.abit.abitmorehex.casting.actions.eval.OpWhileTrue
import net.abit.abitmorehex.casting.actions.math.OpRemoveEveryNth
import net.abit.abitmorehex.casting.actions.math.OpRandomizeList
import net.abit.abitmorehex.casting.actions.math.OpReplaceEveryNth
import net.abit.abitmorehex.casting.actions.rw.OpReadSub
import net.abit.abitmorehex.casting.actions.rw.OpWriteSub
import net.abit.abitmorehex.casting.actions.rw.armor.*
import net.abit.abitmorehex.casting.actions.spells.*
import net.abit.abitmorehex.casting.actions.subcast.rw.OpReadCurrentSub
import net.abit.abitmorehex.casting.actions.subcast.rw.OpWriteCurrentSub
import net.abit.abitmorehex.casting.actions.subiota.OpGetCurrentSlot

object AbitmorehexActions : AbitmorehexRegistrar<ActionRegistryEntry>(
    HexRegistries.ACTION,
    { HexActions.REGISTRY },
) {

    val EMPTYDICT = make("emptydict", HexDir.NORTH_EAST, "qaqdweeew", OpEmptyDict)
    val INDEXDICT = make("indexdict", HexDir.EAST, "dawqqqwaqw", OpIndex)
    val GETINDEXDICT = make("getindexdict", HexDir.WEST, "eqwaeawqaw", OpGetIndex)

    val THOUGHTCLEAR = make("thoughtclear", HexDir.NORTH_WEST, "adadadeaqqq", OpRemoveEveryNth)
    val THOUGHTCLUTTER = make("thoughtclutter", HexDir.NORTH_EAST, "dadadawedqdew", OpReplaceEveryNth)
    val RANDOMIZELIST = make("randomizelist", HexDir.NORTH_WEST, "adwqqqqae", OpRandomizeList)

    val READHELMET = make("head/read", HexDir.EAST, "waqqwawwada", OpReadPinkBow)
    val READCHESTPLATE = make("chest/read", HexDir.EAST, "waqqwawwadq", OpReadCroppedHoodie)
    val READLEGGINS = make("legs/read", HexDir.EAST, "waqqwawwadw", OpReadStockings)
    val READBOOTS = make("feet/read", HexDir.EAST, "waqqwawwade", OpReadHeels)
    val WRITEHELMET = make("head/write", HexDir.EAST, "wedwwdweeqa", OpWritePinkBow)
    val WRITECHESTPLATE = make("chest/write", HexDir.EAST, "wedwwdweeqq", OpWriteCroppedHoodie)
    val WRITELEGGINS = make("legs/write", HexDir.EAST, "wedwwdweeqw", OpWriteStockings)
    val WRITEBOOTS = make("feet/write", HexDir.EAST, "wedwwdweeqe", OpWriteHeels)
    val WRITEABLEHELMET = make("head/writeable", HexDir.EAST, "wedwwdweeqwa", OpWritablePinkBow)
    val WRITEABLECHESTPLATE = make("chest/writeable", HexDir.EAST, "wedwwdweeqwq", OpWritableCroppedHoodie)
    val WRITEABLELEGGINS = make("legs/writeable", HexDir.EAST, "wedwwdweeqww", OpWritableStockings)
    val WRITEABLEBOOTS = make("feet/writeable", HexDir.EAST, "wedwwdweeqwe", OpWritableHeels)
    val READABLEHELMET = make("head/readable", HexDir.EAST, "waqqwawwadwa", OpReadablePinkBow)
    val READABLECHESTPLATE = make("chest/readable", HexDir.EAST, "waqqwawwadwq", OpReadableCroppedHoodie)
    val READABLELEGGINS = make("legs/readable", HexDir.EAST, "waqqwawwadww", OpReadableStockings)
    val READABLEBOOTS = make("feet/readable", HexDir.EAST, "waqqwawwadwe", OpReadableHeels)

    val READSUBIOTA = make("readsubiota", HexDir.EAST, "aaqawqqqq", OpReadSub)
    val WRITESUBIOTA = make("writesubiota", HexDir.NORTH_EAST, "ddeeeeewd", OpWriteSub)

    val OPGETSLOT = make("getitemslot", HexDir.NORTH_EAST, "qaqqaea", OpGetCurrentSlot)
    val READCURRENTSUBIOTA = make("readcurrentsubiota", HexDir.EAST, "aqqwaqdqa", OpReadCurrentSub)
    val WRITECURRENTSUBIOTA = make("writecurrentsubiota", HexDir.WEST, "deewdeaed", OpWriteCurrentSub)
    val CASITEMSPELL = make("castitemspell", HexDir.EAST, "deadaed", OpCastItemSpell)
    val CASITEMWITHSPELL = make("castitemwithspell", HexDir.SOUTH_WEST, "aqdadqa", OpCastItemWithSpell)

    val CONTAINERPOS = make("containerpos", HexDir.SOUTH_WEST, "qadaqq", OpContainerPos)

    val WHILETRUE = make("whiletrueloop", HexDir.NORTH_WEST, "edwaqa", OpWhileTrue)

    val MEDIAFYITEM = make("mediafyitem", HexDir.WEST, "waaqaeq", OpMediafyItem)
    val WEAVEITEM = make("weaveitem", HexDir.WEST, "wddedqe", OpWeaveItem)

    val GRASPINGVINES = make("evokegraspingvines", HexDir.NORTH_WEST, "qqqqqawqadawada", OpEvokeGraspingVines)
    val LEECHINGVINES = make("evokeleechingvines", HexDir.NORTH_WEST, "qqwaeaeaqwaqq", OpEvokeLeechingVines)

    val MINORTELEPORT = make("minorteleport", HexDir.EAST, "waqqqqqeawqwqwaadqdqd", OpMinorTeleport)

    val ACTIVATEROOTS = make("activateroottable", HexDir.SOUTH_EAST, "qqadeqa", OpActivateAltar)
    val CHARGEALTAR = make("chargealtar", HexDir.WEST, "wawawqadawadawada", OpChargeAltar)

    //Altar Actions
    val INFUSIONCRAFTING = make("infusioncrafting", HexDir.NORTH_WEST, "qqqqqawwdeqdqedadqdqd", OpInfusionCrafting)

    private fun make(name: String, startDir: HexDir, signature: String, action: Action) =
        make(name, startDir, signature) { action }

    private fun make(name: String, startDir: HexDir, signature: String, getAction: () -> Action) = register(name) {
        ActionRegistryEntry(HexPattern.fromAngles(signature, startDir), getAction())
    }
}
