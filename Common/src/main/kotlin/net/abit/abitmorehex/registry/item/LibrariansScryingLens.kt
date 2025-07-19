package net.abit.abitmorehex.registry.item

import at.petrak.hexcasting.annotations.SoftImplement
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.utils.getCompound
import at.petrak.hexcasting.api.utils.putTag
import at.petrak.hexcasting.common.items.HexBaubleItem
import at.petrak.hexcasting.common.items.storage.ItemFocus
import at.petrak.hexcasting.common.lib.HexAttributes
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import java.util.*

class LibrariansScryingLens(pProperties: Properties?) : Item(pProperties!!), IotaHolderItem, HexBaubleItem {
    // Wearable,
    // The 0.1 is *additive*
    val GRID_ZOOM = AttributeModifier(
        UUID.fromString("8fa9c3b6-bd5b-4d17-bdcf-b0af780757bc"),
        "Librarians Lens Zoom", 0.66, AttributeModifier.Operation.MULTIPLY_BASE
    )

    val SCRY_SIGHT = AttributeModifier(
        UUID.fromString("1f4885c2-0bff-4bbe-8edc-61d12078d1ec"),
        "Librarians Lens Sight", 1.0, AttributeModifier.Operation.ADDITION
    )

    override fun getDefaultAttributeModifiers(slot: EquipmentSlot): Multimap<Attribute, AttributeModifier> {
        val out = HashMultimap.create(super.getDefaultAttributeModifiers(slot))
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            out.put(HexAttributes.GRID_ZOOM, this.GRID_ZOOM)
            out.put(HexAttributes.SCRY_SIGHT, this.SCRY_SIGHT)
        }
        return out
    }

    override fun getHexBaubleAttrs(stack: ItemStack?): Multimap<Attribute, AttributeModifier>? {
        val out = HashMultimap.create<Attribute, AttributeModifier>()
        out.put(HexAttributes.GRID_ZOOM, this.GRID_ZOOM)
        out.put(HexAttributes.SCRY_SIGHT, this.SCRY_SIGHT)
        return out
    }

    override fun readIotaTag(stack: ItemStack): CompoundTag? {
        return stack.getCompound(ItemFocus.TAG_DATA)
    }


    override fun writeable(stack: ItemStack?): Boolean {
        return true
    }

    override fun canWrite(
        stack: ItemStack?,
        iota: Iota?
    ): Boolean {
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

    @SoftImplement("forge")
    fun getEquipmentSlot(stack: ItemStack?): EquipmentSlot? {
        return EquipmentSlot.HEAD
    }

    override fun appendHoverText(
        pStack: ItemStack,
        pLevel: Level?,
        pTooltipComponents: MutableList<Component>,
        pIsAdvanced: TooltipFlag
    ) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced)
    }

}