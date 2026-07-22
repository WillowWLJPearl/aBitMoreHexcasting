package net.abit.abitmorehex.forge.client

import at.petrak.hexcasting.api.item.IotaHolderItem
import net.abit.abitmorehex.registry.AbitmoreItems
import net.abit.abitmorehex.registry.item.Thought
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.resources.ResourceLocation

object AbitmorehexClientPredicates {
    fun registerAll() {
        val parchment = AbitmoreItems.ANCIENT_PARCHMENT.value
        val holder = parchment as IotaHolderItem

        ItemProperties.register(
            parchment,
            ResourceLocation("abitmorehex", "has_data"),
            ClampedItemPropertyFunction { stack, _, _, _ ->
                if (holder.readIotaTag(stack) != null) 1f else 0f
            }
        )
        ItemProperties.register(
            AbitmoreItems.THOUGHT.value,
            ResourceLocation("abitmorehex", "state")
        ) { stack, _, _, _ ->
            Thought.getState(stack).coerceIn(0.0f, 1.0f)
        }

    }
}