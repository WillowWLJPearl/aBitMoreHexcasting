package net.abit.abitmorehex.blocks

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.common.lib.HexItems
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.abit.abitmorehex.networking.msg.SoulJarParticles
import net.minecraft.Util
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.util.Random
import kotlin.math.max

class SoulJar(props: Properties) : Block(props) {
    // ── jar shape omitted for brevity ──
    val SHAPE: VoxelShape = Shapes.or(
        Block.box(4.0, 10.1, 4.0, 12.0, 11.15, 12.0),  // top plate
        Block.box(3.0, 0.0, 3.0, 13.0, 1.05, 13.0),    // base slab
        Block.box(6.5, 11.0, 6.5, 9.5, 12.0, 9.5),     // inner post 1
        Block.box(6.5, 12.0, 6.5, 9.5, 13.0, 9.5),     // inner post 2
        Block.box(4.0, 1.0, 3.0, 12.0, 11.0, 4.0),     // back beam
        Block.box(4.0, 1.0, 12.0, 12.0, 11.0, 13.0),   // front beam
        Block.box(3.0, 1.0, 4.0, 4.0, 11.0, 12.0),     // left pillar
        Block.box(12.0, 1.0, 4.0, 13.0, 11.0, 12.0)    // right pillar
    )

    override fun getShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape? {
        return SHAPE
    }

    override fun getCollisionShape(
        state: BlockState,
        level: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape? {
        return SHAPE
    }
    override fun getVisualShape(s: BlockState, g: BlockGetter, p: BlockPos, c: CollisionContext) = Shapes.empty()
    override fun getOcclusionShape(s: BlockState, g: BlockGetter, p: BlockPos) = Shapes.empty()
    override fun useShapeForLightOcclusion(s: BlockState) = false
    /* ── server ticking paths ── */



    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, rand: RandomSource) {
        if (!level.isClientSide) return

        // inner cavity bounds (block-local 0..1)
        val minX = 4.1/16.0; val maxX = 11.9/16.0
        val minY = 1.2/16.0; val maxY = 10.0/16.0
        val minZ = 4.1/16.0; val maxZ = 11.9/16.0
        val eps  = 0.01

        // bias to the **center** of the jar
        val cx = pos.x + 0.5
        val cy = pos.y + (minY + maxY) * 0.5
        val cz = pos.z + 0.5
        val spanX = ((maxX - minX) * 0.5) - eps
        val spanY = ((maxY - minY) * 0.5) - eps
        val spanZ = ((maxZ - minZ) * 0.5) - eps

        val count = 10 + rand.nextInt(4) // way more
        repeat(count) {
            fun centered(span: Double): Double {
                val sign = if (rand.nextBoolean()) 1.0 else -1.0
                val mag  = Math.pow(rand.nextDouble(), 3.0) // heavy center bias
                return sign * span * mag
            }
            val x = cx + centered(spanX)
            val y = cy + centered(spanY)
            val z = cz + centered(spanZ)

            // map Y within the cavity to 0..1 (low=0, high=1)
            val y01 = ((y - (pos.y + minY)) / (maxY - minY)).coerceIn(0.0, 1.0)
            val color = lerpBlueFactor(y01.toFloat(), rand) // darker at bottom, lighter at top

            val vy = 0.003 + rand.nextDouble() * 0.003
            level.addParticle(ConjureParticleOptions(color), x, y, z, 0.0, vy, 0.0)
        }
    }

    // Keep your RRGGBBAA packing; choose darkness based on factor (0..1)
    private fun lerpBlueFactor(f: Float, r: RandomSource): Int {
        // base dark→light stops
        val dr=0x10; val dg=0x30; val db=0xA0
        val lr=0x88; val lg=0xC7; val lb=0xFF
        // small random jitter so it’s not perfectly banded
        val j = (r.nextFloat() - 0.5f) * 0.08f
        val t = (f + j).coerceIn(0f, 1f)

        val rr = (dr + (lr - dr) * t).toInt()
        val gg = (dg + (lg - dg) * t).toInt()
        val bb = (db + (lb - db) * t).toInt()
        return (rr shl 24) or (gg shl 16) or (bb shl 8) or 0xFF
    }




    /* ── helper ── */
}

