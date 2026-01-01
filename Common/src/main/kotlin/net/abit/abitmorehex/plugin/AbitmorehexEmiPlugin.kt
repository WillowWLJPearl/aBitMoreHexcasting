package net.abit.abitmorehex.plugin

import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiStack
import net.abit.abitmorehex.client.AbitmorehexEmiRecipe
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import net.abit.abitmorehex.registry.AbitmoreItems
import net.abit.abitmorehex.registry.AbitmorehexRecipeTypes
import net.abit.abitmorehex.registry.AbitmorehexRecipes
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import java.lang.reflect.Method


@EmiEntrypoint
open class AbitmorehexEmiPlugin: EmiPlugin {
    companion object {
        var SPRITE: ResourceLocation = ResourceLocation("abitmorehex", "textures/gui/emi_simplified_textures.png")
        var INFUSION_ALTAR : EmiStack = EmiStack.of(AbitmoreItems.ROOTEDTABLEITEM.value)
        var INFUSION_CATEGORY : EmiRecipeCategory = EmiRecipeCategory(
            ResourceLocation("abitmorehex", "rooted_table_item"),
            INFUSION_ALTAR,
            EmiTexture(SPRITE, 0, 0, 16, 16)
        )
    }

    override fun register(registry: EmiRegistry) {
        registry.addCategory(INFUSION_CATEGORY)
        registry.addWorkstation(INFUSION_CATEGORY, INFUSION_ALTAR)

        val manager = registry.recipeManager as RecipeManager

        // pulls a List<InfusionRecipe> no matter what the obf name is:
        manager
            .getAllOfType(AbitmorehexRecipeTypes.INFUSION.value)
            .forEach { infusion ->
                registry.addRecipe(AbitmorehexEmiRecipe(infusion))
            }
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T : Recipe<*>> RecipeManager.getAllOfType(type: RecipeType<T>): List<T> {
        // find the single method (obf’d in MojMap) that takes a RecipeType and returns a List
        val listMeth: Method = this::class.java.declaredMethods
            .first { m ->
                m.parameterCount == 1 &&
                        RecipeType::class.java.isAssignableFrom(m.parameterTypes[0]) &&
                        List::class.java.isAssignableFrom(m.returnType)
            }.apply { isAccessible = true }

        return listMeth.invoke(this, type) as List<T>
    }




}