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
import net.abit.abitmorehex.misc.ItemTrackerData
import net.abit.abitmorehex.blockentities.RootedTable
import net.abit.abitmorehex.casting.iota.ItemIota as AbitItemIota
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import net.abit.abitmorehex.registry.eval.AltarCastingEnvironment
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import com.google.gson.JsonObject
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

object OpInfusionCrafting : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if (env !is AltarCastingEnvironment) throw MishapNoSpellCircle()
        val vec3 = env.getAlterPos()
        val world = env.world as ServerLevel
        val tablePos = BlockPos.containing(vec3.x, vec3.y, vec3.z)

        val be = world.getBlockEntity(tablePos) as? RootedTable
        val baseStack = be?.getPrimary() ?: run {
            val dud = ItemEntity(world, vec3.x, vec3.y + 0.5, vec3.z, ItemStack.EMPTY)
            throw MishapBadItem(dud, Component.translatable("abitmorehex.mishap.no_recipe_base"))
        }

        // Collect raw elements (ItemIota or EntityIota)
        val rawInputs = (args.getOrNull(0) as? ListIota)?.list ?: emptyList()

        // Separate tracked iotas and dropped entities
        val iotaPairs = rawInputs.filterIsInstance<AbitItemIota>().mapNotNull { iota ->
            ItemTrackerData.get(world).map[iota.id]?.let { stack -> iota to stack.copy() }
        }.toMutableList()
        val entityPairs = rawInputs.mapNotNull { it as? EntityIota }
            .mapNotNull { ei -> (ei.entity as? ItemEntity)?.let { ent -> ent to ent.item.copy() } }
            .toMutableList()

        // Load and validate recipes
        val recipes = world.recipeManager.recipes.filterIsInstance<InfusionRecipe>()
        recipes.firstOrNull { it.base.test(baseStack) } ?: run {
            val culprit = ItemEntity(world, vec3.x, vec3.y + 0.5, vec3.z, baseStack.copy())
            throw MishapBadItem(culprit, Component.translatable("abitmorehex.mishap.no_recipe_base"))
        }

        // Match recipe by required ingredients
        val match = recipes.firstOrNull { r ->
            r.base.test(baseStack) && r.ingredients.all { req ->
                iotaPairs.any { req.test(it.second) } || entityPairs.any { req.test(it.second) }
            }
        } ?: run {
            val nugget = ItemEntity(world, vec3.x, vec3.y + 0.5, vec3.z, ItemStack(Items.GOLD_NUGGET))
            throw MishapBadItem(nugget, Component.translatable("abitmorehex.mishap.no_recipe_ingredient"))
        }

        // Identify extras and cost
        val matchedExtras = match.extras.filter { extra ->
            iotaPairs.any { extra.test(it.second) } || entityPairs.any { extra.test(it.second) }
        }
        val cost = matchedExtras.size.toDouble().pow(2).toLong()

        return Spell(vec3, match, iotaPairs, entityPairs, matchedExtras).let { res ->
            SpellAction.Result(res, cost, listOf(ParticleSpray.cloud(vec3, 1.0)))
        }
    }

    private data class Spell(
        val pos: Vec3,
        val recipe: InfusionRecipe,
        val iotaPairs: MutableList<Pair<AbitItemIota, ItemStack>>,
        val entityPairs: MutableList<Pair<ItemEntity, ItemStack>>,
        val matchedExtras: List<Ingredient>
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val level = env.world as ServerLevel
            val tablePos = BlockPos.containing(pos.x, pos.y, pos.z)
            val be = level.getBlockEntity(tablePos) as? RootedTable
            val data = ItemTrackerData.get(level)

            // Build output and stack NBT based on total extras provided
            val out = recipe.output.copy()

            recipe.extraAttributes?.entrySet()?.forEach { entry ->
                val extraKey = entry.key
                val elt = entry.value.asJsonObject

                // how many of this extra are present (by item path)
                fun countFor(path: String): Int {
                    val reg = level.registryAccess().registryOrThrow(Registries.ITEM)
                    var c = 0
                    for ((_, st) in iotaPairs) if (reg.getKey(st.item)?.path == path) c += st.count
                    for ((_, st) in entityPairs) if (reg.getKey(st.item)?.path == path) c += st.count
                    return c
                }

                val totalCount = countFor(extraKey)
                if (totalCount <= 0) return@forEach   // none present → skip

                val nbtElt = elt.get("nbt") ?: return@forEach  // recipe entry has no "nbt" → skip

                when {
                    // JSON object: scale any top-level numeric fields by totalCount (your old behavior)
                    nbtElt.isJsonObject -> {
                        val obj = nbtElt.asJsonObject
                        obj.entrySet().forEach { (k, v) ->
                            if (v.isJsonPrimitive && v.asJsonPrimitive.isNumber) {
                                val perItem = v.asLong
                                val existing = if (out.orCreateTag.contains(k)) out.orCreateTag.getLong(k) else 0L
                                out.orCreateTag.putLong(k, existing + perItem * totalCount)
                            }
                        }
                    }

                    // SNBT string: parse and merge; optionally scale AttributeModifiers' Amount
                    nbtElt.isJsonPrimitive && nbtElt.asJsonPrimitive.isString -> {
                        val snbt = nbtElt.asString
                        val add = net.minecraft.nbt.TagParser.parseTag(snbt)

                        // If this is an AttributeModifiers compound, multiply Amount by totalCount
                        val mods = add.getList("AttributeModifiers", 10) // 10 = compound
                        for (i in 0 until mods.size) {
                            val comp = mods.getCompound(i)
                            if (comp.contains("Amount", 6)) { // 6 = double
                                val amt = comp.getDouble("Amount")
                                comp.putDouble("Amount", amt * totalCount)
                            }
                        }

                        out.orCreateTag.merge(add) // 1.20+: deep-merge compound keys
                    }

                    else -> {
                        // neither object nor string → ignore this entry safely
                    }
                }
            }


            // Consume required ingredients exactly
            recipe.ingredients.forEach { req ->
                val needed = req.items.firstOrNull()?.count ?: 1
                // try tracked iotas
                val iIdx = iotaPairs.indexOfFirst { req.test(it.second) }
                if (iIdx >= 0) {
                    val (iota, stack) = iotaPairs.removeAt(iIdx)
                    stack.shrink(needed)
                    if (stack.isEmpty) data.map.remove(iota.id) else data.map[iota.id] = stack
                } else {
                    // try dropped entities
                    val eIdx = entityPairs.indexOfFirst { req.test(it.second) }
                    if (eIdx >= 0) {
                        val (entity, stack) = entityPairs.removeAt(eIdx)
                        stack.shrink(needed)
                        if (stack.isEmpty) entity.discard() else entity.item = stack
                    }
                }
            }

            // Remove all extras (tracked and dropped)
            matchedExtras.forEach { extra ->
                iotaPairs.filter { extra.test(it.second) }
                    .forEach { pair -> data.map.remove(pair.first.id) }
                iotaPairs.removeAll { extra.test(it.second) }
                entityPairs.filter { extra.test(it.second) }
                    .forEach { pair -> pair.first.discard() }
                entityPairs.removeAll { extra.test(it.second) }
            }
            data.setDirty()

            // Place result
            be?.let {
                it.setPrimary(out)
                it.setChanged()
            }
        }
    }
}

// Merge arbitrary JSON NBT into the result stack
fun mergeNbtFromJson(stack: ItemStack, json: JsonObject) {
    val tag = stack.orCreateTag
    json.entrySet().forEach { entry ->
        val key = entry.key
        val v: JsonElement = entry.value
        if (v.isJsonPrimitive) {
            val prim: JsonPrimitive = v.asJsonPrimitive
            when {
                prim.isNumber -> tag.putLong(key, prim.getAsLong())
                prim.isString -> tag.putString(key, prim.getAsString())
            }
        }
    }
    stack.tag = tag
}
