package net.abitmorehex.casting.patterns.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import net.minecraft.block.entity.BlockEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos

class OpCompareBlocks : ConstMediaAction {
    override val argc = 2

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val pos1 = (args[0] as Vec3Iota).vec3
        val pos2 = (args[1] as Vec3Iota).vec3

        val blockPos1 = BlockPos(pos1.x.toInt(), pos1.y.toInt(), pos1.z.toInt())
        val blockPos2 = BlockPos(pos2.x.toInt(), pos2.y.toInt(), pos2.z.toInt())

        val blockState1 = ctx.world.getBlockState(blockPos1)
        val blockState2 = ctx.world.getBlockState(blockPos2)

        // Compare block types
        if (blockState1.block != blockState2.block) {
            return listOf(BooleanIota(false))  // Blocks don't match
        }

        // Check for NBT data
        val blockEntity1: BlockEntity? = ctx.world.getBlockEntity(blockPos1)
        val blockEntity2: BlockEntity? = ctx.world.getBlockEntity(blockPos2)

        if (blockEntity1 != null && blockEntity2 != null) {
            val nbt1 = blockEntity1.createNbt()  // Use createNbt() to get the NBT data
            val nbt2 = blockEntity2.createNbt()

            return if (nbt1 == nbt2) {
                listOf(BooleanIota(true), BooleanIota(true))  // Block and NBT match
            } else {
                listOf(BooleanIota(true), BooleanIota(false)) // Block matches, NBT doesn't
            }
        } else if (blockEntity1 == null && blockEntity2 == null) {
            return listOf(BooleanIota(true))  // Blocks match, no NBT
        }

        // Block matches, but one has NBT and the other doesn't
        return listOf(BooleanIota(false), BooleanIota(true))
    }
}
