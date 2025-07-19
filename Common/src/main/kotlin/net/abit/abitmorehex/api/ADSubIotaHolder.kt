package net.abit.abitmorehex.api

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel

interface ADSubIotaHolder {
    fun readIotaTag(): CompoundTag?

    fun readIota(world: ServerLevel?): Iota? {
        val tag = readIotaTag()
        return if (tag != null) {
            IotaType.deserialize(tag, world)
        } else {
            null
        }
    }

    fun emptyIota(): Iota? {
        return null
    }

    /**
     * @return if the writing succeeded/would succeed
     */
    fun writeSubIota(iota: Iota?, simulate: Boolean): Boolean

    /**
     * @return whether it is possible to write to this IotaHolder
     */
    fun writeable(): Boolean
}