package net.abit.abitmorehex.blockentities

import net.abit.abitmorehex.registry.AbitmorehexBlockEntities.ROOTED_TABLE
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState

/**
 * A table that holds two raw ItemStack fields (iota + primary) and a media long.
 */
open class RootedTable(
    type: BlockEntityType<*>,
    pos: BlockPos,
    state: BlockState
) : BlockEntity(type, pos, state) {
    constructor(pos: BlockPos, state: BlockState)
            : this(ROOTED_TABLE.value, pos, state)

    companion object {
        private const val TAG_PRIMARY = "Primary"
        private const val TAG_IOTA    = "Iota"
        private const val TAG_MEDIA   = "Media"
        private const val TAG_MISHAP   = "Mishap"
    }

    private var primaryStack: ItemStack = ItemStack.EMPTY
    private var iotaStack:    ItemStack = ItemStack.EMPTY
    private var media:        Long      = 0L
    private var lastmishap:        Component      = Component.literal("")

    // ---- NBT Persistence ----

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.put(TAG_PRIMARY, primaryStack.save(CompoundTag()))
        tag.put(TAG_IOTA,    iotaStack.save(CompoundTag()))
        tag.putLong(TAG_MEDIA, media)
        tag.putString(TAG_MISHAP, lastmishap.string)
    }

    override fun load(tag: CompoundTag) {
        super.load(tag)
        primaryStack = if (tag.contains(TAG_PRIMARY, 10))
            ItemStack.of(tag.getCompound(TAG_PRIMARY))
        else ItemStack.EMPTY

        iotaStack = if (tag.contains(TAG_IOTA, 10))
            ItemStack.of(tag.getCompound(TAG_IOTA))
        else ItemStack.EMPTY

        media = tag.getLong(TAG_MEDIA)

        lastmishap = Component.literal(tag.getString(TAG_MISHAP))
    }

    // ---- Client Sync ----

    override fun getUpdateTag(): CompoundTag =
        saveWithFullMetadata()

    override fun getUpdatePacket(): ClientboundBlockEntityDataPacket =
        ClientboundBlockEntityDataPacket.create(this)

    // ---- Simple getters/setters ----

    fun getPrimary(): ItemStack = primaryStack.copy()
    fun setPrimary(stack: ItemStack) {
        primaryStack = stack.copy()
        markDirtyAndSync()
    }

    fun getIotaSlot(): ItemStack = iotaStack.copy()
    fun setIotaSlot(stack: ItemStack) {
        iotaStack = stack.copy()
        markDirtyAndSync()
    }

    fun getMedia(): Long = media
    fun setMedia(amount: Long) {
        media = amount
        markDirtyAndSync()
    }
    fun getMishap(): Long = media
    fun setMishap(mishap: Component) {
        lastmishap = mishap
        markDirtyAndSync()
    }

    // ---- Helper ----

    private fun markDirtyAndSync() {
        setChanged()
        level?.sendBlockUpdated(worldPosition, blockState, blockState, 3)
    }
}
