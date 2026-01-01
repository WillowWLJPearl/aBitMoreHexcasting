package net.abit.abitmorehex.recipetypes

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import net.abit.abitmorehex.blockentities.RootedTable
import net.abit.abitmorehex.registry.AbitmorehexRecipeTypes
import net.abit.abitmorehex.registry.AbitmorehexRecipes
import net.minecraft.core.RegistryAccess
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.inventory.CraftingContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.CraftingRecipe
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.crafting.ShapedRecipe
import net.minecraft.world.level.Level

class InfusionRecipe(
    private val id: ResourceLocation,
    val base: Ingredient,
    val ingredients: List<Ingredient>,
    val extras: List<Ingredient>,
    val output: ItemStack,
    val extraAttributes: JsonObject?
) : Recipe<InfusionContainer> {
    override fun matches(container: InfusionContainer, level: Level): Boolean =
        false

    override fun assemble(container: InfusionContainer, registryAccess: RegistryAccess): ItemStack =
        output.copy()

    override fun canCraftInDimensions(width: Int, height: Int): Boolean = true

    override fun getResultItem(registryAccess: RegistryAccess): ItemStack =
        output.copy()

    override fun getId(): ResourceLocation = id

    override fun getSerializer(): RecipeSerializer<*> =
        AbitmorehexRecipes.INFUSION_SERIALIZER.value

    override fun getType(): RecipeType<*>? {
        return AbitmorehexRecipeTypes.INFUSION.value
    }


    override fun getToastSymbol(): ItemStack =
        base.items.firstOrNull() ?: ItemStack.EMPTY

    companion object Serializer : RecipeSerializer<InfusionRecipe> {
        override fun fromJson(id: ResourceLocation, json: JsonObject): InfusionRecipe {
            val base        = Ingredient.fromJson(json.getAsJsonObject("base"))
            val ingredients = json.getAsJsonArray("ingredients")
                .map { Ingredient.fromJson(it) }
            val extras      = json.getAsJsonArray("extras")
                .map { Ingredient.fromJson(it) }
            val outputObj   = json.getAsJsonObject("output")
            val output      = ShapedRecipe.itemStackFromJson(outputObj)
            val extraAttrs  = json.getAsJsonObject("extraAttributes")  // may be null
            return InfusionRecipe(id, base, ingredients, extras, output, extraAttrs)
        }

        override fun fromNetwork(id: ResourceLocation, buf: FriendlyByteBuf): InfusionRecipe {
            val base        = Ingredient.fromNetwork(buf)
            val ingrCount   = buf.readVarInt()
            val ingredients = List(ingrCount) { Ingredient.fromNetwork(buf) }
            val extraCount  = buf.readVarInt()
            val extras      = List(extraCount) { Ingredient.fromNetwork(buf) }
            val output      = buf.readItem()
            val hasAttrs    = buf.readBoolean()
            val extraAttrs  = if (hasAttrs) {
                // we assume you wrote JSON as NBT string
                val nbt = buf.readNbt()!!
                JsonParser.parseString(nbt.getString("json")).asJsonObject
            } else null

            return InfusionRecipe(id, base, ingredients, extras, output, extraAttrs)
        }

        override fun toNetwork(buf: FriendlyByteBuf, recipe: InfusionRecipe) {
            recipe.base.toNetwork(buf)
            buf.writeVarInt(recipe.ingredients.size)
            recipe.ingredients.forEach { it.toNetwork(buf) }

            buf.writeVarInt(recipe.extras.size)
            recipe.extras.forEach { it.toNetwork(buf) }

            buf.writeItem(recipe.output)

            if (recipe.extraAttributes != null) {
                buf.writeBoolean(true)
                val jsonString = recipe.extraAttributes.toString()
                // wrap in NBT so we can round-trip it
                val tag = CompoundTag().apply { putString("json", jsonString) }
                buf.writeNbt(tag)
            } else {
                buf.writeBoolean(false)
            }
        }
    }
}
