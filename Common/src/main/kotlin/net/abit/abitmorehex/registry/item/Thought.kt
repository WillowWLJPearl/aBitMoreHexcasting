package net.abit.abitmorehex.registry.item

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.ExecutionClientView
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.ContinuationIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.PatternIota
import at.petrak.hexcasting.api.casting.math.HexPattern
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.mod.HexTags
import at.petrak.hexcasting.api.utils.getCompound
import at.petrak.hexcasting.api.utils.putTag
import at.petrak.hexcasting.api.utils.underline
import at.petrak.hexcasting.common.items.storage.ItemFocus
import at.petrak.hexcasting.common.lib.hex.HexActions
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.common.msgs.MsgNewSpellPatternS2C
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.abit.abitmorehex.misc.SubIotaHolderItem
import net.abit.abitmorehex.registry.eval.FreeCastingEnvironment
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.abit.abitmorehex.registry.item.ItemShiftingMedia.Companion.findSlotByReference
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Thought(properties: Properties) : Item(properties), IotaHolderItem {
    companion object {
        private const val TAG_CONSUME_NEXT_TICK = "abitmorehex:consume_next_tick"


        private const val TAG = "Size"

        fun getState(stack: ItemStack): Float =
            stack.tag?.getFloat(TAG) ?: 0.10f

        fun setState(stack: ItemStack, state: Float) {
            stack.orCreateTag.putFloat(TAG, state)
        }

    }
    /*helpers*/
    fun countTagsDeep(root: Tag): Int {
        var count = 0
        val stack = ArrayDeque<Tag>()
        stack.add(root)

        while (stack.isNotEmpty()) {
            val el = stack.removeLast()
            count++

            when (el) {
                is CompoundTag -> {
                    for (k in el.allKeys) {
                        el.get(k)?.let(stack::add)
                    }
                }
                is ListTag -> {
                    for (i in 0 until el.size) {
                        stack.add(el[i])
                    }
                }
            }
            // For byte/int/long arrays: counts the array tag as 1 (doesn't count elements).
        }

        return count
    }
    fun nbtTypeName(id: Int) = when (id) {
        0 -> "END"
        1 -> "BYTE"
        2 -> "SHORT"
        3 -> "INT"
        4 -> "LONG"
        5 -> "FLOAT"
        6 -> "DOUBLE"
        7 -> "BYTE_ARRAY"
        8 -> "STRING"
        9 -> "LIST"
        10 -> "COMPOUND"
        11 -> "INT_ARRAY"
        12 -> "LONG_ARRAY"
        else -> "UNKNOWN($id)"
    }

    private fun hasData(stack: ItemStack): Boolean =
        (stack.tag?.get(ItemFocus.TAG_DATA) as? CompoundTag)?.isEmpty == false

    private fun dataLength(stack: ItemStack): Int {
        if(hasData(stack)) {
           val tag = stack.tag?.get(ItemFocus.TAG_DATA) as? CompoundTag
            return countTagsDeep(tag as Tag)
        } else {
            return 0
        }
    }


    private fun canWriteNow(stack: ItemStack?): Boolean = false

    /*IotaHolderItem*/

    override fun readIotaTag(stack: ItemStack): CompoundTag? {
        val root = stack.tag ?: return null
        if (!root.contains(ItemFocus.TAG_DATA)) return null

        // If TAG_DATA is a compound you stored:
        val c = root.getCompound(ItemFocus.TAG_DATA)
        return if (c.isEmpty) null else c
    }

    override fun readIota(stack: ItemStack, world: ServerLevel): Iota? {
        val tag = readIotaTag(stack)?.copy() ?: return emptyIota(stack)
        val iota = IotaType.deserialize(tag, world)

        // don't shrink now; just mark it
        stack.orCreateTag.putBoolean(TAG_CONSUME_NEXT_TICK, true)
        return iota
    }

    override fun writeable(stack: ItemStack?): Boolean = canWriteNow(stack)


    override fun canWrite(stack: ItemStack?, iota: Iota?): Boolean =
        false

    override fun writeDatum(stack: ItemStack, datum: Iota?) {
        if (!canWriteNow(stack) || datum == null) return
        val tag = stack.orCreateTag
        tag.put(ItemFocus.TAG_DATA, IotaType.serialize(datum))
    }
    override fun getName(stack: ItemStack): Component {
        if (!hasData(stack)) {
            setState(stack, 0.10f)
            return Component.translatable("item.abitmorehex.thought.empty")
        }

        val length = dataLength(stack)

        return when {
            length < 10 -> {
                setState(stack, 0.25f)
                Component.translatable("item.abitmorehex.thought.small")
            }
            length < 20 -> {
                setState(stack, 0.50f)
                super.getName(stack)
            }
            length < 50 -> {
                setState(stack, 0.50f)
                Component.translatable("item.abitmorehex.thought.big")
            }
            length < 200 -> {
                setState(stack, 0.75f)
                Component.translatable("item.abitmorehex.thought.ingenious")
            }
            else -> {
                setState(stack, 1.00f)
                Component.translatable("item.abitmorehex.thought.incomprehensible")
            }
        }
    }


    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slot: Int, selected: Boolean) {
        if (level.isClientSide) return

        val tag = stack.tag ?: return
        if (!tag.getBoolean(TAG_CONSUME_NEXT_TICK)) return

        tag.remove(TAG_CONSUME_NEXT_TICK)


            val serverLevel = level as ServerLevel
            val serverPlayer = entity as ServerPlayer

            val hand = serverPlayer.usedItemHand
        println(hand)
            val castvm = IXplatAbstractions.INSTANCE.getStaffcastVM(serverPlayer, hand)
            val listkaputt = HexActions.SPLAT.prototype.asActionResult
            println(listkaputt.first().serialize())

        val newcastvm = CastingVM(castvm.image, FreeCastingEnvironment(serverLevel, serverPlayer, stack))
          val cast = newcastvm.queueExecuteAndWrapIotas(iotas = listkaputt, world = serverLevel)


        IXplatAbstractions.INSTANCE.setStaffcastImage(serverPlayer, newcastvm.image)
        IXplatAbstractions.INSTANCE.sendPacketToPlayer(serverPlayer,
        MsgNewSpellPatternS2C(cast, cast.resolutionType.ordinal))
            stack.shrink(1)
    }
}