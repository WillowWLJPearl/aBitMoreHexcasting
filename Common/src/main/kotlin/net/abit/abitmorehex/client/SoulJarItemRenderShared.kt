
@file:JvmName("SoulJarItemRenderShared")
@file:Suppress("DEPRECATION")

package net.abit.abitmorehex.client

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import kotlin.math.cos
import kotlin.math.sin

object SoulJarItemRenderShared {
    private val mc get() = Minecraft.getInstance()
    private val itemRenderer get() = mc.itemRenderer

    // your particle sprite (32x32 or 16x16 png)
    private val particleTex = ResourceLocation("hexcasting", "textures/particle/soul_glow.png")

    @JvmStatic
    fun render(stack: ItemStack, ctx: ItemDisplayContext, pose: PoseStack, buf: MultiBufferSource, light: Int, overlay: Int) {
        // 1) render the baked 3D block model like vanilla
        val model = itemRenderer.getModel(stack, null, null, 0)
        itemRenderer.render(stack, ctx, false, pose, buf, light, overlay, model)

        // 2) draw “particles” (billboard quads) in the GUI
        renderBillboardRing(pose, buf, light)
    }

    private fun renderBillboardRing(pose: PoseStack, buf: MultiBufferSource, light: Int) {
        val verts = buf.getBuffer(RenderType.entityTranslucentCull(particleTex))
        val t = (System.currentTimeMillis() % 2600L) / 2600f
        val count = 10
        val radius = 0.25f
        val size = 0.06f

        pose.pushPose()
        // roughly around the jar neck
        pose.translate(0.5, 0.7, 0.5)

        val poseMat = pose.last().pose()
        val normal = pose.last().normal()

        fun v(x: Float, y: Float, z: Float, u: Float, v: Float) =
            verts.vertex(poseMat, x, y, z)
                .color(255, 255, 255, 200)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0f, 1f, 0f)
                .endVertex()

        for (i in 0 until count) {
            val ang = (i / count.toFloat()) * (Math.PI.toFloat() * 2f) + t * (Math.PI.toFloat() * 2f)
            val x = cos(ang) * radius
            val z = sin(ang) * radius
            val s = size

            // simple camera-facing-ish quad is fine for item GUI
            v(x - s,  s, z, 0f, 0f)
            v(x + s,  s, z, 1f, 0f)
            v(x + s, -s, z, 1f, 1f)
            v(x - s, -s, z, 0f, 1f)
        }

        pose.popPose()
    }
}
