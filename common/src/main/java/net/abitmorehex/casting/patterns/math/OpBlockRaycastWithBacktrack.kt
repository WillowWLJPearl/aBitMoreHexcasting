package net.abitmorehex.casting.patterns.math

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.*
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

class OpBlockRaycastWithBacktrack : ConstMediaAction {
    override val argc = 3
    override val mediaCost = MediaConstants.DUST_UNIT / 100

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val origin = (args[0] as Vec3Iota).vec3
        val direction = (args[1] as Vec3Iota).vec3.normalize() // Use look as the direction
        val forwardDistance = (args[2] as DoubleIota).double

        ctx.assertVecInRange(origin)

        var currentPos = origin
        var blockHitResult: BlockPos? = null

        // Manual raycast by stepping along the vector
        for (i in 0..100) { // Assuming a max range of 100 blocks
            val stepPos = currentPos.add(direction.multiply(1.0)) // Step in the provided direction
            val blockPos = BlockPos(stepPos)
            if (!ctx.world.isAir(blockPos)) {
                blockHitResult = blockPos
                break
            }
            currentPos = stepPos
        }

        // Initialize the list of blocks starting with the hit block
        val blocks = mutableListOf<Iota>()

        return if (blockHitResult != null && ctx.isVecInRange(Vec3d.ofCenter(blockHitResult))) {
            // Add the first hit block to the list
            blocks.add(Vec3Iota(Vec3d.ofCenter(blockHitResult)))
            var forwardPos = Vec3d.ofCenter(blockHitResult)

            for (i in 1..forwardDistance.toInt()) {
                // Move forward along the direction
                forwardPos = forwardPos.add(direction.multiply(1.0))
                val blockPos = BlockPos(forwardPos.x, forwardPos.y, forwardPos.z)
                if (ctx.isVecInRange(Vec3d.ofCenter(blockPos))) {
                    blocks.add(Vec3Iota(Vec3d.ofCenter(blockPos)))
                }
            }
            listOf(ListIota(blocks))  // Return a single ListIota containing all positions
        } else {
            listOf(NullIota())
        }
    }
}
