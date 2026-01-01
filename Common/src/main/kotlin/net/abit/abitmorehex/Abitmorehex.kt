package net.abit.abitmorehex

import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import net.abit.abitmorehex.config.AbitmorehexConfig
import net.abit.abitmorehex.misc.LootInjector
import net.abit.abitmorehex.networking.AbitmorehexNetworking
import net.abit.abitmorehex.recipes.AbitmorehexRecipesAdditions
import net.abit.abitmorehex.registry.*
import net.abit.abitmorehex.registry.AbitmorehexActions

object Abitmorehex {

    const val MODID = "abitmorehex"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)

    @JvmStatic
    fun id(path: String) = ResourceLocation(MODID, path)

    fun init() {
        AbitmorehexConfig.init()
        initRegistries(
            AbitmorehexIotaTypes,
            AbitmorehexActions,
            AbitmorehexEffects,
            AbitmorehexBlocks,
            AbitmoreItems,
            AbitmorehexBlockEntities,
            AbitmorehexCreativeTabRegistries,
            AbitmorehexRecipeTypes,
            AbitmorehexRecipes
        )
        LootInjector.init()
        AbitmorehexNetworking.init()
    }
}
