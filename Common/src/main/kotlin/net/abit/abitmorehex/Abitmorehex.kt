package net.abit.abitmorehex

import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import net.abit.abitmorehex.config.AbitmorehexConfig
import net.abit.abitmorehex.networking.AbitmorehexNetworking
import net.abit.abitmorehex.networking.msg.CastSuccessMessage
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
            AbitmoreItems,
            AbitmorehexBlocks,
            AbitmorehexBlockEntities,
            AbitmorehexRecipeTypes,
            AbitmorehexRecipes
        )
        AbitmorehexNetworking.init()
    }
}
