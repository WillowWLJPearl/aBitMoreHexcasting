package net.abit.abitmorehex

import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.common.items.pigment.ItemPridePigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import me.shedaniel.autoconfig.AutoConfig
import net.abit.abitmorehex.config.AbitmorehexConfig
import net.abit.abitmorehex.config.AbitmorehexConfig.GlobalConfig
import net.abit.abitmorehex.networking.AbitmorehexNetworking
import net.abit.abitmorehex.networking.msg.CastFailureMessage
import net.abit.abitmorehex.networking.msg.CastSuccessMessage
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import java.util.*


object AbitmorehexClient {
    fun init() {
        AbitmorehexConfig.initClient()
        CastFailureMessage.register(AbitmorehexNetworking.CHANNEL)
        CastSuccessMessage.register(AbitmorehexNetworking.CHANNEL)
    }

    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(GlobalConfig::class.java, parent).get()
    }


    fun getTypeFor(): ResolvedPatternType {
        // 1) Look up which pigment the player has
        val pigment = IXplatAbstractions.INSTANCE.getPigment(Minecraft.getInstance().player)
        val pigmentItem = pigment.item.item as ItemPridePigment   // this is an ItemPridePigment.Type
        val pigmentType = pigmentItem.type
        // 2) Convert its name to the enum constant name
        val enumName = pigmentType.name.uppercase(Locale.ROOT)

        // 3) Use valueOf to fetch the matching ResolvedPatternType
        return try {
            ResolvedPatternType.valueOf(enumName)
        } catch (e: IllegalArgumentException) {
            // fallback if something went wrong
            ResolvedPatternType.EVALUATED
        }
    }

}
