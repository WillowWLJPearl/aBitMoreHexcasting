package net.abit.abitmorehex.registry

import at.petrak.hexcasting.api.HexAPI.modLoc
import at.petrak.hexcasting.common.items.ItemStaff
import at.petrak.hexcasting.common.lib.HexCreativeTabs
import at.petrak.hexcasting.common.lib.HexItems.props
import net.abit.abitmorehex.registry.item.AkashicWoodCirclet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.Item



object AbitmoreItems

    : AbitmorehexRegistrar<Item>(
    Registries.ITEM,         // your custom effect registry key
    { BuiltInRegistries.ITEM }           // supplier for the registry instance
) {
val MIKU_STAFF = register("staff/miku") { ItemStaff(unstackable()) }

    val AKASHIC_WOOD_CIRCLET = register("akashic_wood_circlet") { AkashicWoodCirclet(
        material = ArmorMaterials.LEATHER,
        type = ArmorItem.Type.HELMET,
        properties = unstackable()
    ) }


    fun unstackable(): Item.Properties {
        return props().stacksTo(1)
    }
}
