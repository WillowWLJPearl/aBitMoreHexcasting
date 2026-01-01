package net.abit.abitmorehex.registry.item

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.utils.getCompound
import at.petrak.hexcasting.api.utils.putTag
import at.petrak.hexcasting.api.utils.underline
import at.petrak.hexcasting.common.items.storage.ItemFocus
import net.abit.abitmorehex.misc.SubIotaHolderItem
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class StitchedScribbles(properties: Properties) : Item(properties), IotaHolderItem,SubIotaHolderItem {
    override fun readSubIotaTag(stack: ItemStack): CompoundTag? {
        return stack.getCompound(SubIotaHolderItem.TAG_DATA)
    }

    override fun writeablesub(stack: ItemStack): Boolean {
        return true
    }

    override fun canWritesub(stack: ItemStack, iota: Iota?): Boolean {
        return true
    }

    override fun writesubDatum(stack: ItemStack, iota: Iota?) {
        if (iota == null) {
            stack.removeTagKey(SubIotaHolderItem.TAG_DATA)
        } else if (!ItemFocus.isSealed(stack)) {
            stack.putTag(SubIotaHolderItem.TAG_DATA, IotaType.serialize(iota))
        }
    }

    override fun readIotaTag(stack: ItemStack): CompoundTag? {
        return stack.getCompound(ItemFocus.TAG_DATA)
    }

    override fun writeable(stack: ItemStack?): Boolean {
        return true
    }

    override fun canWrite(stack: ItemStack?, iota: Iota?): Boolean {
       return true
    }

    override fun writeDatum(stack: ItemStack, datum: Iota?) {
        if (datum == null) {
            stack.removeTagKey(ItemFocus.TAG_DATA)
            stack.removeTagKey(ItemFocus.TAG_SEALED)
        } else if (!ItemFocus.isSealed(stack)) {
            stack.putTag(ItemFocus.TAG_DATA, IotaType.serialize(datum))
        }
    }

    override fun appendHoverText(
        pStack: ItemStack,
        pLevel: Level?,
        pTooltipComponents: MutableList<Component>,
        pIsAdvanced: TooltipFlag
    ) {
        pTooltipComponents.add(Component.literal("Primary").withStyle(ChatFormatting.LIGHT_PURPLE).underline)
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced)
        pTooltipComponents.add(Component.literal("Secondary").withStyle(ChatFormatting.DARK_PURPLE).underline)
        SubIotaHolderItem.appendSecondaryHoverText(this, pStack, pTooltipComponents, pIsAdvanced)
    }

}