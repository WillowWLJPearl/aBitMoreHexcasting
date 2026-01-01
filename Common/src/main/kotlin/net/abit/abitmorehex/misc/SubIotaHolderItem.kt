package net.abit.abitmorehex.misc

import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.utils.hasString
import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtUtils
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

interface SubIotaHolderItem {
    companion object {
        const val TAG_OVERRIDE_VISUALLY = "VisualOverride"
        // fallback error color if nothing is stored
        const val DEFAULT_ERROR_COLOR = 0xFFFF0000.toInt()

        const val TAG_DATA = "bdata"


        fun appendSecondaryHoverText(
            self: SubIotaHolderItem, stack: ItemStack, components: MutableList<Component>,
            flag: TooltipFlag
        ) {
            val datumTag = self.readSubIotaTag(stack)
            if (datumTag != null) {
                val cmp = IotaType.getDisplay(datumTag)
                components.add(Component.translatable("hexcasting.spelldata.onitem", cmp))
                if (flag.isAdvanced) {
                    components.add(Component.literal("").append(NbtUtils.toPrettyComponent(datumTag)))
                }
            } else if (stack.hasString(SubIotaHolderItem.TAG_OVERRIDE_VISUALLY)) {
                components.add(
                    Component.translatable(
                        "hexcasting.spelldata.onitem",
                        Component.translatable("hexcasting.spelldata.anything").withStyle(ChatFormatting.DARK_PURPLE)
                    )
                )
            }
        }
    }

    /** Read the raw iota NBT, or null if none. */
    fun readSubIotaTag(stack: ItemStack): CompoundTag?

    /** Deserialize an Iota from NBT in a server context. */
    fun readSubIota(stack: ItemStack, world: ServerLevel): Iota? {
        val dh = stack.item
        require(dh is SubIotaHolderItem) {
            "Item $dh does not implement SubIotaHolderItem"
        }
        val tag = (dh as SubIotaHolderItem).readSubIotaTag(stack) ?: return null
        return IotaType.deserialize(tag, world)
    }

    /** What this contains when no iota is present. */
    fun bemptyIota(stack: ItemStack): Iota? = null

    /** Compute the display color, possibly cycling or based on override. */
    fun bgetColor(stack: ItemStack): Int {
        val tag = stack.tag
        if (tag != null && tag.contains(TAG_OVERRIDE_VISUALLY, /*type=*/8)) {
            val override = tag.getString(TAG_OVERRIDE_VISUALLY)
            if (override.isNotBlank() &&
                ResourceLocation.isValidResourceLocation(override)) {
                HexIotaTypes.REGISTRY[ResourceLocation(override)]?.let {
                    return it.color()
                }
            }
            // rainbow cycle fallback
            val hue = (ClientTickCounter.getTotal() % 360) / 360f
            return 0xFF000000.toInt() or (Mth.hsvToRgb(hue, 0.75f, 1f) and 0x00FFFFFF)
        }

        val dataTag = readSubIotaTag(stack) ?: return DEFAULT_ERROR_COLOR
        return IotaType.getColor(dataTag)
    }

    /** Whether writing is allowed at all. */
    fun writeablesub(stack: ItemStack): Boolean

    /** Whether writing this particular Iota is allowed. */
    fun canWritesub(stack: ItemStack, iota: Iota?): Boolean

    /** Perform the write (or erase) of the given Iota. */
    fun writesubDatum(stack: ItemStack, iota: Iota?)
    /**
     * Append lore based on stored Iota or visual override.
     * Call from your Item’s `appendHoverText`.
     */
    fun appendHoverText(
        stack: ItemStack,
        components: MutableList<Component>,
        flag: TooltipFlag
    ) {
        val datumTag = readSubIotaTag(stack)
        if (datumTag != null) {
            val cmp = IotaType.getDisplay(datumTag)
            components.add(Component.translatable("hexcasting.spelldata.onitem", cmp))
            if (flag.isAdvanced) {
                components.add(
                    Component.literal("")
                        .append(NbtUtils.toPrettyComponent(datumTag))
                )
            }
        } else if (stack.tag?.contains(TAG_OVERRIDE_VISUALLY, /*type=*/8) == true) {
            components.add(
                Component.translatable(
                    "hexcasting.spelldata.onitem",
                    Component.translatable("hexcasting.spelldata.anything")
                        .withStyle(ChatFormatting.WHITE)
                )
            )
        }
    }
}
