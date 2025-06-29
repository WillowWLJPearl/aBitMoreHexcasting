package net.abit.abitmorehex.fabric

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.abit.abitmorehex.AbitmorehexClient

object FabricAbitmorehexModMenu : ModMenuApi {
    override fun getModConfigScreenFactory() = ConfigScreenFactory(AbitmorehexClient::getConfigScreen)
}
