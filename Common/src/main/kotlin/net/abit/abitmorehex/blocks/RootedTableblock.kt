package net.abit.abitmorehex.blocks

import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.common.blocks.circles.BlockEmptyImpetus
import net.abit.abitmorehex.blockentities.RootedTable
import net.abit.abitmorehex.registry.AbitmorehexBlockEntities.ROOTED_TABLE
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.phys.shapes.VoxelShape

class RootedTableBlock(settings: Properties)
    : HorizontalDirectionalBlock(settings), EntityBlock {

    val SHAPE: VoxelShape = Shapes.or(
        // base
        Block.box(1.0, 0.0, 1.0, 15.0, 1.0, 15.0),
        Block.box(2.0, 1.0, 2.0, 14.0, 2.0, 14.0),

        // lower pillars / frames
        Block.box(2.0,  2.0,  2.0,  3.0, 12.0,  4.0),
        Block.box(3.0,  2.0,  2.0,  4.0, 12.0,  3.0),
        Block.box(12.0, 2.0,  2.0, 13.0, 12.0,  3.0),
        Block.box(12.0, 2.0, 13.0, 13.0, 12.0, 14.0),
        Block.box(3.0,  2.0, 13.0,  4.0, 12.0, 14.0),
        Block.box(13.0, 2.0,  2.0, 14.0, 12.0,  4.0),
        Block.box(2.0,  2.0, 12.0,  3.0, 12.0, 14.0),
        Block.box(13.0, 2.0, 12.0, 14.0, 12.0, 14.0),

        // top ring + caps
        Block.box(2.0, 12.0,  2.0, 14.0, 13.0, 14.0),
        Block.box(3.0, 13.0,  3.0, 13.0, 14.0, 13.0),
        Block.box(2.0, 13.0,  2.0,  3.0, 16.0,  3.0),
        Block.box(2.0, 13.0, 13.0,  3.0, 16.0, 14.0),
        Block.box(13.0,13.0,  2.0, 14.0, 16.0,  3.0),
        Block.box(13.0,13.0, 13.0, 14.0, 16.0, 14.0),
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

    init {
        this.registerDefaultState(
            this.defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)
        )
    }

    override fun createBlockStateDefinition(
        builder: StateDefinition.Builder<Block, BlockState>
    ) {
        builder.add(BlockStateProperties.HORIZONTAL_FACING)
    }

    /** This is called by Minecraft when your block is placed. */
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        // Call the two-arg constructor on your BE
        RootedTable(pos, state)                                                  // matches EntityBlock#newBlockEntity :contentReference[oaicite:5]{index=5}

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection.opposite)

    override fun rotate(state: BlockState, rot: Rotation) =
        state.setValue(FACING, rot.rotate(state.getValue(FACING)))

    override fun mirror(state: BlockState, mirror: Mirror) =
        state.rotate(mirror.getRotation(state.getValue(FACING)))

    override fun use(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hand: InteractionHand,
        hit: BlockHitResult
    ): InteractionResult {
        if (world.isClientSide) return InteractionResult.SUCCESS

        val be     = world.getBlockEntity(pos) as? RootedTable ?: return InteractionResult.PASS
        val inHand = player.getItemInHand(hand)
        val slot0  = be.getPrimary()
        val slot1  = be.getIotaSlot()

        // Compute local Y (0.0–1.0) inside the block space
        val localY = hit.location.y - pos.y
        val topHalf = localY >= 0.5 // flip this if you want the other mapping

        if (topHalf) {
            // BOTTOM HALF → primary slot
            if (slot0.isEmpty && !inHand.isEmpty) {
                be.setPrimary(inHand.split(1))
                return InteractionResult.CONSUME
            }
            if (!slot0.isEmpty && inHand.isEmpty) {
                player.setItemInHand(hand, slot0.copy())
                be.setPrimary(ItemStack.EMPTY)
                return InteractionResult.CONSUME
            }
            return InteractionResult.PASS
        } else {
            // TOP HALF → iota slot (only accepts IotaHolderItem)
            if (slot1.isEmpty && !inHand.isEmpty && inHand.item is IotaHolderItem) {
                be.setIotaSlot(inHand.split(1))
                return InteractionResult.CONSUME
            }
            if (!slot1.isEmpty && inHand.isEmpty) {
                player.setItemInHand(hand, slot1.copy())
                be.setIotaSlot(ItemStack.EMPTY)
                return InteractionResult.CONSUME
            }
            return InteractionResult.PASS
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, newState: BlockState, isMoving: Boolean) {
        if (!state.equals(newState.block)) {
            (level.getBlockEntity(pos) as? RootedTable)?.let { be ->
                net.minecraft.world.Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, be.getPrimary())
                net.minecraft.world.Containers.dropItemStack(level, pos.x + 0.5, pos.y + 0.5, pos.z + 0.5, be.getIotaSlot())
            }
            super.onRemove(state, level, pos, newState, isMoving)
        } else {
            super.onRemove(state, level, pos, newState, isMoving)
        }
    }





}
