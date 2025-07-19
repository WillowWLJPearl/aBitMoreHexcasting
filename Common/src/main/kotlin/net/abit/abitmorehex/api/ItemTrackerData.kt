package net.abit.abitmorehex.api

import at.petrak.hexcasting.api.utils.serializeToNBT
import net.minecraft.client.Minecraft
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.item.ItemStack
import java.util.UUID
import net.minecraft.world.level.Level

class ItemTrackerData : SavedData() {
    var map = mutableMapOf<UUID, ItemStack>()

    /** Called by Minecraft when saving.  */
    override fun save(tag: CompoundTag): CompoundTag {
        val list = ListTag()
        for ((uuid, stack) in map) {
            val entry = CompoundTag().also {
                it.putString("uuid", uuid.toString())
                it.put("stack", stack.serializeToNBT())
            }
            list.add(entry)
        }
        tag.put("entries", list)
        return tag
    }

    companion object {
        private const val NAME = "abitmorehex_item_tracker"

        /**
         * Get (or create) the *global* ItemTrackerData for this world.
         * computeIfAbsent(reader, factory, name):
         *  • reader   = how to deserialize from existing NBT
         *  • factory  = how to construct a new empty instance
         *  • NAME     = the filename under world/data
         */
        fun getGlobal(level: Level): ItemTrackerData? {
            // 1) If it’s already a ServerLevel, use it
            if (level is ServerLevel) {
                return ItemTrackerData.get(level)
            }
            // 2) Otherwise it’s a ClientLevel (or DimensionType), so pull up the integrated server
            val mc = Minecraft.getInstance()
            val server = mc.singleplayerServer    // null on a dedicated client
            val overworld = server?.getLevel(level.dimension())  // or server.overworld
            return overworld?.let { ItemTrackerData.get(it) }
        }
        fun get(world: ServerLevel ): ItemTrackerData {
            return world.dataStorage.computeIfAbsent(
                { nbt ->
                    // reader: rebuild a ItemTrackerData from NBT
                    val data = ItemTrackerData()
                    val list = nbt.getList("entries", /*TAG_COMPOUND=*/10)
                    for (i in 0 until list.size) {
                        val entry = list.getCompound(i)
                        val id   = UUID.fromString(entry.getString("uuid"))
                        val stack = ItemStack.of(entry.getCompound("stack"))
                        data.map[id] = stack
                    }
                    data
                },
                { ItemTrackerData() },
                NAME
            )
        }
    }
}
