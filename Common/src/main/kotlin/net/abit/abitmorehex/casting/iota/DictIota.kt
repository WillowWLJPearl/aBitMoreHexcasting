package net.abit.abitmorehex.casting.iota

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.MutableComponent

class DictIota(
    private val map: Map<Iota, Iota>
) : Iota(TYPE, map) {

    override fun isTruthy() = map.isNotEmpty()

    override fun toleratesOther(that: Iota?): Boolean {
        if (that !is DictIota) return false
        if (this.map.keys != that.map.keys) return false
        return this.map.all { (k, v1) ->
            Iota.tolerates(v1, that.map[k]!!)
        }
    }

    fun asMap(): Map<Iota,Iota> = this.map

    override fun size(): Int = map.values.sumOf { it.size() } + 1

    override fun depth(): Int = (map.values.maxOfOrNull { it.depth() } ?: 0) + 1


    override fun serialize(): Tag {
        // Write out as a ListTag of { "k": keyTag, "v": valueTag } entries
        val out = ListTag()
        for ((keyIota, valIota) in map) {
            val pair = CompoundTag()
            pair.put("k", IotaType.serialize(keyIota))
            pair.put("v", IotaType.serialize(valIota))
            out.add(pair)
        }
        return out
    }


    fun get(key: Iota): Iota? {
        // Serialize the lookup key once
        val keyTag = IotaType.serialize(key)
        // Find the first entry whose serialized key matches
        return map.entries.firstOrNull { (k, _) ->
            IotaType.serialize(k) == keyTag
        }?.value
    }


    override fun subIotas(): Iterable<Iota>? = map.values


    companion object {
        private val colorStyle = ChatFormatting.GOLD

        val TYPE: IotaType<DictIota> = object : IotaType<DictIota>() {
            override fun deserialize(tag: Tag, world: ServerLevel): DictIota? {
                val list = tag as? ListTag ?: return null
                val m = mutableMapOf<Iota, Iota>()
                for (elem in list) {
                    val pair = elem as? CompoundTag ?: return null
                    val kt = pair.get("k") as? CompoundTag ?: return null
                    val vt = pair.get("v") as? CompoundTag ?: return null
                    val keyIota = IotaType.deserialize(kt, world) ?: return null
                    val valIota = IotaType.deserialize(vt, world) ?: return null
                    m[keyIota] = valIota
                }
                return DictIota(m)
            }

            override fun display(tag: Tag): Component {
                // 1) Treat tag as your ListTag of pairs:
                val listTag = tag as? ListTag
                    ?: return Component.literal("{}").withStyle(colorStyle)
                if (listTag.isEmpty())
                    return Component.literal("{}").withStyle(colorStyle)

                // 2) Build one MutableComponent per {k => v} entry
                var out: MutableComponent = Component.literal("")
                for (i in 0 until listTag.size) {
                    val pair = listTag.getCompound(i)
                    val keyTag = pair.getCompound("k")
                    val valTag = pair.getCompound("v")

                    // 3) Render each side using the correct Iota display
                    out = out
                        .append(IotaType.getDisplay(keyTag))
                        .append(" → ")
                        .append(IotaType.getDisplay(valTag))

                    // 4) Add comma between entries
                    if (i < listTag.size - 1) {
                        out = out.append(", ")
                    }
                }

                // 5) Wrap in braces and style
                return Component.literal("{")
                    .append(out)
                    .append("}")
                    .withStyle(colorStyle)
            }




            override fun color() = 0xFFFFA500.toInt()  // pure orange
        }
    }

}
