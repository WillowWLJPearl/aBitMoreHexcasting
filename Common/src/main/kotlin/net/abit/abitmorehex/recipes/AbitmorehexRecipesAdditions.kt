package net.abit.abitmorehex.recipes

import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.common.recipe.ingredient.StateIngredientHelper
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.EntityTypeIngredient
import at.petrak.hexcasting.common.recipe.ingredient.brainsweep.VillagerIngredient
import at.petrak.hexcasting.datagen.recipe.builders.BrainsweepRecipeBuilder
import net.abit.abitmorehex.registry.AbitmorehexBlocks
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import java.util.function.Consumer

class AbitmorehexRecipesAdditions(output: PackOutput) : RecipeProvider(output) {
    override fun buildRecipes(consumer: java.util.function.Consumer<FinishedRecipe>) {
        BrainsweepRecipeBuilder(
            StateIngredientHelper.of(AbitmorehexBlocks.EMPTYJAR.value),
            EntityTypeIngredient(EntityType.ALLAY),
            AbitmorehexBlocks.SOULJAR.value.defaultBlockState(),
            10000
        )
            .unlockedBy(getHasName(AbitmorehexBlocks.SOULJAR.value), has(AbitmorehexBlocks.SOULJAR.value))
            .save(consumer, ResourceLocation("abitmorehex", "brainsweep/soul_jar"))
    }
}
