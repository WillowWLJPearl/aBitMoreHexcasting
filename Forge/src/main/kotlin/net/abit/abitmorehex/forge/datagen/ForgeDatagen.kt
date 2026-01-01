package net.abit.abitmorehex.forge.datagen

import net.abit.abitmorehex.Abitmorehex.MODID
import net.abit.abitmorehex.recipes.AbitmorehexRecipesAdditions
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
object ForgeDatagen {
    @SubscribeEvent
    fun gatherData(e: net.minecraftforge.data.event.GatherDataEvent) {
        val gen = e.generator
        if (e.includeServer()) {
            gen.addProvider(true, AbitmorehexRecipesAdditions(gen.packOutput))
        }
    }
}
