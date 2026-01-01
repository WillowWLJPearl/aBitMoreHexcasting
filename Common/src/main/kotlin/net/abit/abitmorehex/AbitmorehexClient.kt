package net.abit.abitmorehex

import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.common.items.pigment.ItemPridePigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import dev.architectury.registry.client.rendering.RenderTypeRegistry
import me.shedaniel.autoconfig.AutoConfig
import net.abit.abitmorehex.client.RegisterDisplays
import net.abit.abitmorehex.config.AbitmorehexConfig
import net.abit.abitmorehex.config.AbitmorehexConfig.GlobalConfig
import net.abit.abitmorehex.networking.AbitmorehexNetworking
import net.abit.abitmorehex.networking.msg.CastFailureMessage
import net.abit.abitmorehex.networking.msg.CastSuccessMessage
import net.abit.abitmorehex.networking.msg.SoulJarParticles
import net.abit.abitmorehex.registry.AbitmorehexBlocks
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.RenderType
import java.util.*


object AbitmorehexClient {
    fun init() {

        AbitmorehexConfig.initClient()
        CastFailureMessage.register(AbitmorehexNetworking.CHANNEL)
        CastSuccessMessage.register(AbitmorehexNetworking.CHANNEL)
        SoulJarParticles.register(AbitmorehexNetworking.CHANNEL)
        RegisterDisplays.register()
        RenderTypeRegistry.register(RenderType.cutout(), AbitmorehexBlocks.SOULJAR.value)

    }

    fun getConfigScreen(parent: Screen): Screen {
        return AutoConfig.getConfigScreen(GlobalConfig::class.java, parent).get()
    }

    fun getTypeFor(fallback: ResolvedPatternType): ResolvedPatternType {
        val mc = Minecraft.getInstance()
        val player = mc.player ?: return fallback

        val pigment = IXplatAbstractions.INSTANCE.getPigment(player)
        val item = pigment.item.item

        val pride = item as? ItemPridePigment ?: return fallback

        val enumName = pride.type.name.uppercase(Locale.ROOT)
        return runCatching { ResolvedPatternType.valueOf(enumName) }
            .getOrElse { fallback }
    }


}
