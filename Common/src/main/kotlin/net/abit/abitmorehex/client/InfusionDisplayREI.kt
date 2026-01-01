package net.abit.abitmorehex.client

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.basic.BasicDisplay
import me.shedaniel.rei.api.common.util.EntryIngredients
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import java.util.Optional

class InfusionDisplay(recipe: InfusionRecipe) : BasicDisplay(
    listOf(
        EntryIngredients.ofIngredients(listOf(recipe.base)),
        EntryIngredients.ofIngredients(recipe.ingredients),
        EntryIngredients.ofIngredients(recipe.extras)
    ).flatten(),
    listOf(EntryIngredients.of(recipe.output)),
    Optional.of(recipe.id)
) {
    private val infusionRecipe = recipe
    override fun getCategoryIdentifier() = CATEGORY

    companion object {
        val CATEGORY: CategoryIdentifier<InfusionDisplay> =
            CategoryIdentifier.of("abitmorehex", "infusion")
    }

    // **Helper getters** so your category can split inputs without magic indices
    val requiredCount get() = infusionRecipe.ingredients.size
    val extraCount    get() = infusionRecipe.extras.size

    val requiredStacks
        get() = inputEntries
            .subList(1, 1 + requiredCount)
            .map { it.first() }

    val extraStacks
        get() = inputEntries
            .subList(1 + requiredCount, 1 + requiredCount + extraCount)
            .map { it.first() }
}
