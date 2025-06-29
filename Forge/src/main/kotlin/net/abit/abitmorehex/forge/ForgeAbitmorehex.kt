package net.abit.abitmorehex.forge

import dev.architectury.platform.forge.EventBuses
import net.abit.abitmorehex.Abitmorehex
import net.minecraft.data.DataProvider
import net.minecraft.data.DataProvider.Factory
import net.minecraft.data.PackOutput
import net.minecraftforge.data.event.GatherDataEvent
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(Abitmorehex.MODID)
class AbitmorehexForge {
    init {
        MOD_BUS.apply {
            EventBuses.registerModEventBus(Abitmorehex.MODID, this)
            addListener(ForgeAbitmorehexClient::init)
            addListener(::gatherData)
        }
        Abitmorehex.init()
    }

    private fun gatherData(event: GatherDataEvent) {
        event.apply {
            // TODO: add datagen providers here
        }
    }
}

fun <T : DataProvider> GatherDataEvent.addProvider(run: Boolean, factory: (PackOutput) -> T) =
    generator.addProvider(run, Factory { factory(it) })
