package net.abit.abitmorehex.registry

import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.common.lib.HexRegistries
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.abit.abitmorehex.casting.iota.DictIota
import net.abit.abitmorehex.casting.iota.ItemIota

object AbitmorehexIotaTypes : AbitmorehexRegistrar<IotaType<*>>(
    HexRegistries.IOTA_TYPE,
    { HexIotaTypes.REGISTRY }
) {
    val DICTIOTA: Entry<IotaType<DictIota>> = register("dictiota") { DictIota.TYPE }
    val ITEMIOTA: Entry<IotaType<ItemIota>> = register("itemiota") { ItemIota.TYPE }
}

