package net.abit.abitmorehex.fabric

import at.petrak.hexcasting.api.item.IotaHolderItem
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry
import net.abit.abitmorehex.AbitmorehexClient
import net.abit.abitmorehex.client.RootedTableRenderer
import net.abit.abitmorehex.registry.AbitmoreItems
import net.abit.abitmorehex.registry.AbitmorehexBlockEntities.ROOTED_TABLE
import net.abit.abitmorehex.registry.item.Thought
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.minecraft.resources.ResourceLocation

object FabricAbitmorehexClient : ClientModInitializer {
    override fun onInitializeClient() {
        BlockEntityRendererRegistry.register(ROOTED_TABLE.value, ::RootedTableRenderer)


        val item   = AbitmoreItems.ANCIENT_PARCHMENT.value
        val holder = item as IotaHolderItem

        // predicate id MUST match the JSON key exactly
        val id = ResourceLocation("abitmorehex", "has_data")
        FabricModelPredicateProviderRegistry.register(
            AbitmoreItems.THOUGHT.value,
            ResourceLocation("abitmorehex", "state")
        ) { stack, _, _, _ ->
            Thought.getState(stack)
        }

        FabricModelPredicateProviderRegistry.register(item, id) { stack, _, _, _ ->
            if (holder.readIotaTag(stack) != null) 1f else 0f
        }
    }
}
