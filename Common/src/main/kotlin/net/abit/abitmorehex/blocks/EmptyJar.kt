package net.abit.abitmorehex.blocks

import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape
import java.awt.Shape

class EmptyJar(properties: Properties) : Block(properties) {

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

    override fun getOcclusionShape(state: BlockState, level: BlockGetter, pos: BlockPos): VoxelShape? {
        return SHAPE
    }

    override fun useShapeForLightOcclusion(state: BlockState): Boolean {
        return true
    }
}