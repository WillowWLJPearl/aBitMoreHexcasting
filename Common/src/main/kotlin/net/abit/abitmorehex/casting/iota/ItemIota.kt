package net.abit.abitmorehex.casting.iota

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import net.abit.abitmorehex.misc.ItemTrackerData
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import java.util.UUID

class ItemIota(val id: UUID) : Iota(TYPE, id) {
    override fun isTruthy(): Boolean = true
    override fun toleratesOther(that: Iota?): Boolean =
        that is ItemIota && that.id == this.id

    override fun size(): Int = 1
    override fun depth(): Int = 1
    override fun subIotas(): Iterable<Iota>? = null

    override fun serialize(): Tag =
        // store the raw UUID as a StringTag
        StringTag.valueOf(id.toString())

    companion object {
        private val colorStyle = ChatFormatting.GOLD

        val TYPE: IotaType<ItemIota> = object : IotaType<ItemIota>() {
            override fun deserialize(tag: Tag, world: ServerLevel) =
                (tag as? StringTag)?.asString
                    ?.let { uuidStr -> runCatching { UUID.fromString(uuidStr) }.getOrNull() }
                    ?.let { ItemIota(it) }

            override fun display(tag: Tag): Component {
                val uuidStr = (tag as? StringTag)?.asString
                    ?: return Component.literal("Null").withStyle(colorStyle)

                val uuid = runCatching { UUID.fromString(uuidStr) }.getOrNull()
                    ?: return Component.literal("Null").withStyle(colorStyle)

                return Component.literal("[item!$uuid]").withStyle(colorStyle)
            }

            override fun color() = 0xFF00BFFF.toInt()  // light blue
        }
    }
}
