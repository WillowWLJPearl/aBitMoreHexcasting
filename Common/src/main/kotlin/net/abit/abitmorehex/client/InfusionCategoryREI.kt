package net.abit.abitmorehex.client

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryStacks
import net.abit.abitmorehex.registry.AbitmoreItems
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Items
import kotlin.math.cos
import kotlin.math.sin

class InfusionCategory : DisplayCategory<InfusionDisplay> {
    override fun getCategoryIdentifier(): CategoryIdentifier<out InfusionDisplay> =
        InfusionDisplay.CATEGORY

    override fun getTitle() = Component.literal("Infusion")

    override fun getIcon() =
        EntryStacks.of(AbitmoreItems.ROOTEDTABLEITEM.value)

    override fun getDisplayWidth(display: InfusionDisplay): Int {
        val slot    = 18
        val padding = 4
        val req     = display.requiredCount
        // compute radius exactly as in your layout
        val radius = if (req > 0) {
            val circumference = req * (slot + padding)
            (circumference / (2 * Math.PI)).toInt().coerceAtLeast(30)
        } else {
            30
        }
        // width must cover the circle, plus output slot margin, plus any extras row
        val extrasWidth = display.extraCount * slot
        // give a little horizontal margin (20px) on either side
        return (radius * 2 + 20 + extrasWidth).coerceAtLeast(100)+20
    }

    // Remove the 'display' parameter here
    override fun getDisplayHeight(): Int {
        val slot    = 18
        val padding = 4
        // if you want to size for up to N ingredients, you can bake N in here,
        // or just pick the max you ever expect (e.g. 8 around the circle).
        val maxReq = 8
        val circumference = maxReq * (slot + padding)
        val radius = (circumference / (2 * Math.PI)).toInt().coerceAtLeast(30)

        // extras row height (one slot + gap)
        val extrasHeight = slot + 10

        // + top/bottom margin
        return radius * 2 + extrasHeight + 20+20
    }


    override fun setupDisplay(display: InfusionDisplay, bounds: Rectangle): List<Widget> {
        val widgets = mutableListOf<Widget>()
        widgets += Widgets.createRecipeBase(bounds)

        // center coords
        val cx = bounds.x + bounds.width  / 2
        val cy = bounds.y + bounds.height / 2 - 10

        // convenience
        val slot = 18
        val pad  = 4
        val req  = display.requiredStacks
        val reqCount = req.size

        // compute a single radius used for both the circle and for positioning extras/output
        val radius = if (reqCount > 0) {
            val circumference = reqCount * (slot + pad)
            (circumference / (2 * Math.PI)).toInt().coerceAtLeast(30)
        } else {
            30
        }

        // 1) Base in the middle
        val base = display.inputEntries[0].first()
        widgets += Widgets.createSlot(Point(cx - slot/2, cy - slot/2))
            .entry(base)
            .markInput()

        // 2) Required around in a circle
        req.forEachIndexed { i, stack ->
            val angle = 2.0 * Math.PI * i / reqCount
            val x     = cx + (kotlin.math.cos(angle) * radius).toInt() - slot/2
            val y     = cy + (kotlin.math.sin(angle) * radius).toInt() - slot/2
            widgets += Widgets.createSlot(Point(x, y))
                .entry(stack)
                .markInput()
        }

        // 3) Extras in a row underneath the circle
        val extras = display.extraStacks
        if (extras.isNotEmpty()) {
            val extrasY = cy + radius + 20
            val totalW  = extras.size * slot
            val startX  = cx - totalW / 2
            extras.forEachIndexed { j, stack ->
                widgets += Widgets.createSlot(Point(startX + j * slot, extrasY))
                    .entry(stack)
                    .markInput()
            }
        }

        // 4) Output slot to the right
        val outX = cx + radius + 10
        val outY = cy - slot/2
        val output = display.outputEntries[0].first()
        widgets += Widgets.createSlot(Point(outX+5, outY))
            .entry(output)
            .markOutput()

        return widgets
    }





}
