package net.abit.abitmorehex.registry

import net.abit.abitmorehex.Abitmorehex.MODID
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import net.minecraft.core.registries.Registries
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.crafting.RecipeType

object AbitmorehexRecipeTypes
    : AbitmorehexRegistrar<RecipeType<*>>(
    Registries.RECIPE_TYPE,                      // Which registry to use :contentReference[oaicite:4]{index=4}
    { BuiltInRegistries.RECIPE_TYPE }
) {

    /** Register “infusion” during the registry event */
    val INFUSION: Entry<RecipeType<InfusionRecipe>> = register("infusion") {
        // No static calls here—just create an anonymous RecipeType.
        object : RecipeType<InfusionRecipe> {}
    }
}
