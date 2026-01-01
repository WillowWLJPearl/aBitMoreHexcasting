package net.abit.abitmorehex.client

import com.google.gson.JsonObject
import dev.emi.emi.api.recipe.BasicEmiRecipe
import dev.emi.emi.api.recipe.EmiRecipeCategory
import dev.emi.emi.api.render.EmiTexture
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.api.stack.EmiStack
import dev.emi.emi.api.widget.WidgetHolder
import net.abit.abitmorehex.plugin.AbitmorehexEmiPlugin
import net.abit.abitmorehex.recipetypes.InfusionRecipe
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.crafting.Ingredient
import java.util.function.Supplier
import kotlin.math.*

// 1) We only ever construct from an InfusionRecipe;
//    Java will see:
//      public AbitmorehexEmiRecipe(InfusionRecipe recipe) { … }
class AbitmorehexEmiRecipe(
    private val recipe: InfusionRecipe
) : BasicEmiRecipe(
    // delegate up to BasicEmiRecipe:
    AbitmorehexEmiPlugin.INFUSION_CATEGORY,
    recipe.id,
    /* width/height are placeholders; we override getDisplayWidth/Height below */
    0, 0
) {
    private val slotSize    = 18
    private val ringPadding = 4    // gap between slots
    private val arrowGap    = 8    // gap between circle and arrow
    private val arrowTex    = EmiTexture.EMPTY_ARROW

    private fun computeRadii(): Pair<Int,Int> {
        val n          = recipe.ingredients.size.coerceAtLeast(1)
        val chord      = slotSize + ringPadding
        val rawInner   = (chord / (2 * sin(PI / n))).toInt()
        val innerRad   = max(rawInner, slotSize + ringPadding)
        val outerRad   = innerRad + chord
        return innerRad to outerRad
    }

    // 2) Category/ID now come from the recipe or your plugin:
    override fun getCategory() = AbitmorehexEmiPlugin.INFUSION_CATEGORY
    override fun getId() = recipe.id

    // 3) Inputs/outputs you already populated in the ctor–but let's do it here
    init {
        // center/base
        inputs.add(EmiIngredient.of(recipe.base))
        // required ingredients, extras, and output:
        recipe.ingredients.forEach { inputs.add(EmiIngredient.of(it)) }
        recipe.extras     .forEach { inputs.add(EmiIngredient.of(it)) }
        outputs.add(EmiStack.of(recipe.output))
    }

    // 4) Dynamic sizing based on how many circles you need
    override fun getDisplayWidth(): Int {
        val (_, outer) = computeRadii()
        // circle diameter + arrow + output slot
        return (outer * 2 + slotSize) + arrowGap + slotSize+ 10
    }

    override fun getDisplayHeight(): Int {
        val (_, outer) = computeRadii()
        // circle diameter
        return outer * 2 + slotSize+ 10
    }



    override fun addWidgets(widgets: WidgetHolder) {
        val (innerR, outerR) = computeRadii()

        // center of circle‐area at (outerR, outerR)
        val centerX = outerR
        val centerY = outerR +10

        // 1) base in exact centre
        widgets.addSlot(
            EmiIngredient.of(recipe.base),
            centerX - slotSize/2,
            centerY - slotSize/2
        ).recipeContext(this)

        // helper to place a ring
        fun placeRing(items: List<*>, radius: Int) {
            val n    = items.size.coerceAtLeast(1)
            val step = 2 * PI / n
            for ((i, ing) in items.withIndex()) {
                val angle = i * step - PI/2
                val x     = (centerX + cos(angle) * radius).roundToInt() - slotSize/2
                val y     = (centerY + sin(angle) * radius).roundToInt() - slotSize/2
                widgets.addSlot(
                    EmiIngredient.of(ing as net.minecraft.world.item.crafting.Ingredient),
                    x, y
                ).recipeContext(this)
            }
        }

        // 2) inner ring (required)
        placeRing(recipe.ingredients, innerR)

// 3) outer ring (extras) WITH tooltips pulled from recipe.extraAttributes
        val extras: List<Ingredient> = recipe.extras.map { it as Ingredient }
        val nExtras = extras.size
        if (nExtras > 0) {
            val step = 2 * PI / nExtras

            // iterate each extra, place slot, and attach tooltip
            for ((i, ing) in extras.withIndex()) {
                val angle = i * step - PI / 2
                val x = (centerX + cos(angle) * outerR).roundToInt() - slotSize / 2
                val y = (centerY + sin(angle) * outerR).roundToInt() - slotSize / 2

                // place the slot
                widgets.addSlot(EmiIngredient.of(ing), x, y).recipeContext(this)

                // figure out which item key to look up in JSON (e.g., "glowstone_dust")
                val stack = ing.items.firstOrNull() ?: continue
                val item = stack.item
                val keyPath = BuiltInRegistries.ITEM.getKey(item).path


// read JSON: { "<path>": { "nbt": <object OR string SNBT> } }
                val infoElem = recipe.extraAttributes?.get(keyPath)
                val infoObj  = infoElem?.takeIf { it.isJsonObject }?.asJsonObject
                val nbtElem  = infoObj?.get("nbt")

                val tip = mutableListOf<ClientTooltipComponent>()
                tip += ClientTooltipComponent.create(stack.hoverName.visualOrderText)

                when {
                    nbtElem == null -> {
                        tip += ClientTooltipComponent.create(Component.literal("No extra attributes").visualOrderText)
                    }
                    nbtElem.isJsonObject -> {
                        val nbtObj = nbtElem.asJsonObject
                        for ((k, v) in nbtObj.entrySet()) {
                            val vStr = if (v.isJsonPrimitive) v.asJsonPrimitive.toString() else v.toString()
                            tip += ClientTooltipComponent.create(Component.literal("$k: $vStr").visualOrderText)
                        }
                    }
                    nbtElem.isJsonPrimitive && nbtElem.asJsonPrimitive.isString -> {
                        val snbt = nbtElem.asString
                        // Optional: pretty message or parsed keys
                        tip += ClientTooltipComponent.create(Component.literal("SNBT: $snbt").visualOrderText)
                        // If you want parsed key/value lines, you can parse SNBT:
                        // val tag = TagParser.parseTag(snbt)
                        // tip += ClientTooltipComponent.create(Component.literal(tag.toString()).visualOrderText)
                    }
                    else -> {
                        tip += ClientTooltipComponent.create(Component.literal("Unsupported nbt value").visualOrderText)
                    }
                }
                widgets.addTooltip(tip, x, y, slotSize, slotSize)
            }
        }


        // 4) arrow & output
        val arrowX = centerX + outerR + slotSize/2 + ringPadding -15
        val arrowY = centerY - arrowTex.height/2
        widgets.addTexture(arrowTex, arrowX, arrowY)

        val outX = arrowX + arrowTex.width + arrowGap
        widgets.addSlot(
            EmiStack.of(recipe.output),
            outX, centerY - slotSize/2
        ).recipeContext(this)
    }


}
