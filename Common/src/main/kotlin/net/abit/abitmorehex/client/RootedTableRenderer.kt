package net.abit.abitmorehex.client

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.abit.abitmorehex.blockentities.RootedTable
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemDisplayContext
import kotlin.math.sin

class RootedTableRenderer(ctx: BlockEntityRendererProvider.Context)
    : BlockEntityRenderer<RootedTable> {

    override fun render(
        be: RootedTable,
        partialTicks: Float,
        pose: PoseStack,
        buffer: MultiBufferSource,
        packedLight: Int,
        packedOverlay: Int
    ) {
        val level = be.level ?: return
        val light = LevelRenderer.getLightColor(level, be.blockPos.above())
        val itemRenderer = Minecraft.getInstance().itemRenderer

        val t = (level.gameTime % 360_000L).toFloat() + partialTicks
        val spin = (t * 4f) % 360f
        val bob  = 0.10 + 0.03 * sin(t / 10.0)

        fun renderStack(x: Double, y: Double, z: Double, rotY: Float, scale: Float, stackSupplier: () -> net.minecraft.world.item.ItemStack) {
            val stack = stackSupplier()
            if (stack.isEmpty) return

            pose.pushPose()
            pose.translate(x, y + bob, z)
            pose.mulPose(Axis.YP.rotationDegrees(spin + rotY))
            pose.scale(scale, scale, scale)
            itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.GROUND, // “on table” look; use FIXED if you prefer
                light,
                OverlayTexture.NO_OVERLAY,
                pose,
                buffer,
                level,
                0
            )
            pose.popPose()
        }

        // Center positions; adjust if you want them offset toward the block’s front:
        val cx = 0.5
        val cz = 0.5
        val primaryY = 1.05
        val iotaY    = 0.30

        // Optional: face-aware offset (tiny nudge to the front)
        val facing: Direction = be.blockState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)
        val (ox, oz) = when (facing) {
            Direction.NORTH -> 0.0 to -0.05
            Direction.SOUTH -> 0.0 to  0.05
            Direction.WEST  -> -0.05 to 0.0
            Direction.EAST  ->  0.05 to 0.0
            else -> 0.0 to 0.0
        }

        renderStack(cx + ox, primaryY, cz + oz, rotY = 0f,   scale = 0.9f) { be.getPrimary() }
        renderStack(cx - ox, iotaY,    cz - oz, rotY = 15f,  scale = 0.9f) { be.getIotaSlot() }
    }
}