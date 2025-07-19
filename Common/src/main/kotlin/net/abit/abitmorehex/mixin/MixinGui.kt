package net.abit.abitmorehex.mixin

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.GameRenderer
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(net.minecraft.client.gui.Gui::class)
abstract class MixinGui {
    @Inject(
        method  = ["render(Lnet/minecraft/client/gui/GuiGraphics;F)V"], // **exact** JVM signature
        at      = [At("TAIL")],
        remap   = false                                              // disable Yarn/intermediary remapping
    )
    private fun onRender(
        guiGraphics: GuiGraphics,
        partialTick: Float,
        ci: CallbackInfo
    ) {
        // PROVE it’s firing:
        Minecraft.getInstance().window.setTitle("🔥 MixinGui Active 🔥")

        // Then draw your purple overlay:
        RenderSystem.disableDepthTest()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader(GameRenderer::getPositionShader)

        val buf = Tesselator.getInstance().builder
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION)
        buf.vertex(-1.0,  1.0, 0.0).endVertex()
        buf.vertex( 1.0,  1.0, 0.0).endVertex()
        buf.vertex( 1.0, -1.0, 0.0).endVertex()
        buf.vertex(-1.0, -1.0, 0.0).endVertex()
        Tesselator.getInstance().end()

        RenderSystem.disableBlend()
        RenderSystem.enableDepthTest()
    }
}
