package net.abit.abitmorehex

import net.abit.abitmorehex.config.AbitmorehexConfig
import net.abit.abitmorehex.config.AbitmorehexConfig.GlobalConfig
import me.shedaniel.autoconfig.AutoConfig
import net.minecraft.client.gui.screens.Screen

object AbitmorehexClient {
    fun init() {
        AbitmorehexConfig.initClient()
    }

    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(GlobalConfig::class.java, parent).get()
    }
}
