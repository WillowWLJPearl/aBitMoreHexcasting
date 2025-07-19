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
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.*
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.phys.BlockHitResult

class RootedTableBlock(settings: Properties)
    : HorizontalDirectionalBlock(settings), EntityBlock {

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
        val front  = state.getValue(BlockStateProperties.HORIZONTAL_FACING)

        // 1) Non-front faces → only primary slot
        if (hit.direction != front) {
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
        }

        // 2) Front face → only Iota slot
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
