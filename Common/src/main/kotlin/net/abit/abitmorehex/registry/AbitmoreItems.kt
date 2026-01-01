package net.abit.abitmorehex.registry

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.common.items.ItemStaff
import at.petrak.hexcasting.common.lib.HexCreativeTabs
import at.petrak.hexcasting.common.lib.HexItems.props
import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.client.rendering.RenderTypeRegistry
import net.abit.abitmorehex.blocks.RootedTableBlock
import net.abit.abitmorehex.registry.AbitmorehexBlocks.ROOTEDTABLEBLOCK
import net.abit.abitmorehex.registry.item.AkashicWoodCirclet
import net.abit.abitmorehex.registry.item.AncientParchment
import net.abit.abitmorehex.registry.item.ItemShiftingMedia
import net.abit.abitmorehex.registry.item.LibrariansScryingLens
import net.abit.abitmorehex.registry.item.StitchedScribbles
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import java.util.function.Supplier


object AbitmoreItems

    : AbitmorehexRegistrar<Item>(
    Registries.ITEM,         // your custom effect registry key
    { BuiltInRegistries.ITEM }           // supplier for the registry instance
) {
    val MIKU_STAFF = register("staff/miku") { ItemStaff(unstackable()) }

    val SHIFTING_MEDIA = register("shifting_media") { ItemShiftingMedia(unstackable()) }

    val STICHED_SCRIBBLES = register("stitched_scribbles") { StitchedScribbles(unstackable()) }

    val LIBRARIANS_SCRYING_LENS = register("librarians_lens") { LibrariansScryingLens(unstackable()) }

    val ANCIENT_PARCHMENT = register("ancient_parchment") { AncientParchment(unstackable()) }

    val AKASHIC_WOOD_CIRCLET = register("akashic_wood_circlet") { AkashicWoodCirclet(
        properties = unstackable()
    ) }

    val EMPTYJAR = register("empty_jar") {
        BlockItem(AbitmorehexBlocks.EMPTYJAR.value, Item.Properties())
    }
    val SOULJAR = register("soul_jar") {
        BlockItem(AbitmorehexBlocks.SOULJAR.value, Item.Properties())
    }

    val ROOTEDTABLEITEM = register("rooted_table_item") { BlockItem(
        ROOTEDTABLEBLOCK.value,
        Item.Properties()
    )


    }


    fun unstackable(): Item.Properties {
        return props().stacksTo(1)
    }
    override fun initClient() {
        RenderTypeRegistry.register(
            net.minecraft.client.renderer.RenderType.translucent(),
            AbitmorehexBlocks.EMPTYJAR.value,
            AbitmorehexBlocks.SOULJAR.value
        )
    }


}
