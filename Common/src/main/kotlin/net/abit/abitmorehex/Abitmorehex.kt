package net.abit.abitmorehex

import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import net.abit.abitmorehex.config.AbitmorehexConfig
import net.abit.abitmorehex.networking.AbitmorehexNetworking
import net.abit.abitmorehex.registry.AbitmoreItems
import net.abit.abitmorehex.registry.AbitmorehexActions
import net.abit.abitmorehex.registry.AbitmorehexEffects

object Abitmorehex {
    const val MODID = "abitmorehex"

    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MODID)

    @JvmStatic
    fun id(path: String) = ResourceLocation(MODID, path)

    fun init() {
        AbitmorehexConfig.init()
        initRegistries(
            AbitmorehexActions,
            AbitmorehexEffects,
            AbitmoreItems
        )
        AbitmorehexNetworking.init()
    }
}
