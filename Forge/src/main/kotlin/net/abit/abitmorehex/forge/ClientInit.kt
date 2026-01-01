package net.abit.abitmorehex.forge

import net.abit.abitmorehex.forge.client.AbitmorehexClientPredicates
import net.abit.abitmorehex.client.RootedTableRenderer
import net.abit.abitmorehex.registry.AbitmorehexBlockEntities.ROOTED_TABLE
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = [Dist.CLIENT])
object ClientInit {
    @SubscribeEvent
    fun registerRenderers(e: EntityRenderersEvent.RegisterRenderers) {
        e.registerBlockEntityRenderer(ROOTED_TABLE.value) { ctx -> RootedTableRenderer(ctx) }
        AbitmorehexClientPredicates.registerAll()
    }
}