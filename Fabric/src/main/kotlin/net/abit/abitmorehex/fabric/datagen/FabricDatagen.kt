package net.abit.abitmorehex.fabric.datagen

import net.abit.abitmorehex.recipes.AbitmorehexRecipesAdditions
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

class FabricDatagen : DataGeneratorEntrypoint {
    override fun onInitializeDataGenerator(gen: FabricDataGenerator) {
        val pack = gen.createPack()
        pack.addProvider(::AbitmorehexRecipesAdditions)
    }
}