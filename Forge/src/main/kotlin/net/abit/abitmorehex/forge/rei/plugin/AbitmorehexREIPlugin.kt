package net.abit.abitmorehex.forge.rei.plugin

import dev.architectury.event.EventResult
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.util.EntryStacks
import me.shedaniel.rei.forge.REIPluginClient
import me.shedaniel.rei.forge.REIPluginCommon
import net.abit.abitmorehex.client.InfusionCategory
import net.abit.abitmorehex.client.InfusionDisplay
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import net.abit.abitmorehex.registry.AbitmoreItems
import net.abit.abitmorehex.registry.AbitmorehexRecipes
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.RecipeHolder
import net.minecraft.world.item.Items

@REIPluginClient
class AbitmorehexREIPlugin : REIClientPlugin {
    override fun registerDisplays(registry: DisplayRegistry) {
        // Link your recipe class directly to your display
        registry.registerFiller(InfusionRecipe::class.java, ::InfusionDisplay)
    }

    override fun registerCategories(registry: CategoryRegistry) {
        // Register your category
        registry.add(InfusionCategory())

        // Optional: define workstations that show the recipe (like your table block or enchanting table)
        registry.addWorkstations(
            InfusionDisplay.CATEGORY,
            EntryStacks.of(AbitmoreItems.ROOTEDTABLEITEM.value) // Replace with your actual block/item
        )
    }
}