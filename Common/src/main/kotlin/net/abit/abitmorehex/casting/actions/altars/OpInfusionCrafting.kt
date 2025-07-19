package net.abit.abitmorehex.casting.actions.altars

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.common.casting.actions.spells.OpFlight
import com.google.gson.JsonObject
import net.abit.abitmorehex.api.ItemTrackerData
import net.abit.abitmorehex.blockentities.RootedTable
import net.abit.abitmorehex.casting.iota.ItemIota
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import net.abit.abitmorehex.registry.eval.AltarCastingEnvironment
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries.ITEM
import net.minecraft.core.registries.Registries
import net.minecraft.core.registries.Registries.ITEM
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.item.Items
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

object OpInfusionCrafting : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is AltarCastingEnvironment) throw MishapNoSpellCircle()

        val vec3 = env.getAlterPos()
        val world = env.world as ServerLevel
        val tablePos = BlockPos.containing(vec3.x, vec3.y, vec3.z)


        // 1) grab the focus from the table (or mishap)
        val be = world.getBlockEntity(tablePos) as? RootedTable
        val baseStack = be?.getPrimary() ?: run {
            val dud = ItemEntity(world, vec3.x, vec3.y + 0.5, vec3.z, ItemStack.EMPTY)
            throw MishapBadItem(
                dud,
                Component.translatable("abitmorehex.mishap.no_recipe_base")
            )
        }

        // 2) collect dropped item-entities (nuggets, glowstone, etc.)
        val provided = (args.getOrNull(0) as? ListIota)
            ?.list
            ?.mapNotNull { (it as? EntityIota)?.entity as? ItemEntity }
            ?: emptyList()

        // 3) grab all InfusionRecipe instances (avoids RecipeType identity pitfalls)
        val recipes = world.recipeManager
            .recipes
            .filterIsInstance<InfusionRecipe>()

        // 4a) ensure at least one recipe uses this focus as its base
        val validBase = recipes.firstOrNull { it.base.test(baseStack) }
            ?: run {
                val culprit = ItemEntity(world, vec3.x, vec3.y + 0.5, vec3.z, baseStack.copy())
                throw MishapBadItem(
                    culprit,
                    Component.translatable("abitmorehex.mishap.no_recipe_base")
                )
            }


// 2) Collect ItemIotas and resolve to ItemStacks
        val providedIotas = (args.getOrNull(0) as? ListIota)
            ?.list
            ?.mapNotNull { it as? ItemIota }
            ?: emptyList()

        val providedStacks = providedIotas.mapNotNull { iota ->
            ItemTrackerData.get(world).map[iota.id]
        }
        val match = recipes.firstOrNull { r ->
            r.base.test(baseStack) &&
                    r.ingredients.all { req ->
                        providedStacks.any { stack -> req.test(stack) }
                    }
        } ?: run {
            val nugget = ItemEntity(
                world, vec3.x, vec3.y + 0.5, vec3.z,
                ItemStack(Items.GOLD_NUGGET)
            )
            throw MishapBadItem(
                nugget,
                Component.translatable("abitmorehex.mishap.no_recipe_ingredient")
            )
        }

        // 5) collect any optional extras
        val matchedExtras = match.extras.filter { extra ->
            providedStacks.any { stack -> extra.test(stack) }
        }
        // 6) cost calculation unchanged
        val cost = matchedExtras.size.toDouble().pow(2).toLong()

        // 7) pass both the raw Iotas (for later removal if needed) and the resolved stacks
        return SpellAction.Result(
            Spell(vec3, match, providedIotas, providedStacks, matchedExtras),
            cost,
            listOf(ParticleSpray.cloud(vec3, 1.0))
        )
    }

    private data class Spell(
        val pos: Vec3,
        val recipe: InfusionRecipe,
        // the original ItemIotas you collected
        val allIotas: List<ItemIota>,
        // the resolved / actual stacks
        val allStacks: List<ItemStack>,
        // which extras to consume
        val matchedExtras: List<Ingredient>
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val level = env.world as ServerLevel
            val tablePos = BlockPos.containing(pos.x, pos.y, pos.z)
            val be = level.getBlockEntity(tablePos) as? RootedTable

            // 4) Build output and apply extra‐attribute NBT…
            val out = recipe.output.copy()
            val tag = out.orCreateTag
            val itemReg = level.registryAccess()
                .registryOrThrow(Registries.ITEM)

            recipe.extraAttributes
                ?.entrySet()
                ?.forEach { (extraKey, elt) ->
                    // total count of that extra across all dropped stacks
                    val totalCount = allStacks.sumOf { stack ->
                        val itemKey = itemReg.getKey(stack.item)?.path
                        if (itemKey == extraKey) stack.count else 0
                    }
                    if (totalCount > 0) {
                        val nbtJson = elt.asJsonObject.get("nbt").asJsonObject
                        nbtJson.entrySet().forEach { (nbtKey, nbtElt) ->
                            if (nbtElt.isJsonPrimitive && nbtElt.asJsonPrimitive.isNumber) {
                                val perItem = nbtElt.asLong
                                val existing = if (tag.contains(nbtKey)) tag.getLong(nbtKey) else 0L
                                tag.putLong(nbtKey, existing + perItem * totalCount)
                            }
                        }
                    }
                }

            // 5) Place the result back into the altar
            if (be != null) {
                be.setPrimary(out)
                be.setChanged()
                val data = ItemTrackerData.get(level)
                allIotas.forEach { iota ->
                    data.map.remove(iota.id)
                }
                data.setDirty()
            }
        }
    }




}
// Merge arbitrary JSON NBT into the result stack
fun mergeNbtFromJson(stack: ItemStack, json: JsonObject) {
    val tag = stack.orCreateTag
    for ((key, elt) in json.entrySet()) {
        when {
            elt.isJsonPrimitive && elt.asJsonPrimitive.isNumber ->
                tag.putLong(key, elt.asLong)
            elt.isJsonPrimitive && elt.asJsonPrimitive.isString ->
                tag.putString(key, elt.asString)
        }
    }
    stack.tag = tag
}
