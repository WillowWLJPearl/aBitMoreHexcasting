package net.abit.abitmorehex.registry

import net.abit.abitmorehex.Abitmorehex.MODID
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import net.minecraft.core.registries.Registries
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.crafting.RecipeSerializer

object AbitmorehexRecipes
    : AbitmorehexRegistrar<RecipeSerializer<*>>(
    Registries.RECIPE_SERIALIZER,
    { BuiltInRegistries.RECIPE_SERIALIZER }
) {

    val INFUSION_SERIALIZER: Entry<RecipeSerializer<InfusionRecipe>> =
        register("infusion") {
            InfusionRecipe.Serializer
        }
}
