package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.misc.MediaConstants
import net.minecraft.core.BlockPos
import net.minecraft.world.item.BoneMealItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import kotlin.math.ceil
import kotlin.math.floor

object OpWillowsNurturingWhispers : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val center = (args[0] as Vec3Iota).vec3
        env.assertVecInRange(center)
        val radius = (args[1] as DoubleIota).double.toInt()

        // Calculate media cost: here proportional to area (πr²) or volume (4/3πr³)
        // For simplicity, use area times DUST_UNIT:
        val area = Math.PI * radius * radius
        val cost: Long = (0.2 * area * MediaConstants.DUST_UNIT).toLong()
            //1/5th dusts per block off the volume
        val particles = listOf(
            ParticleSpray.cloud(center, radius.toDouble())
        )

        return SpellAction.Result(Spell(center, radius), cost, particles)
    }

    private data class Spell(val center: Vec3, val radius: Int) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val world = env.world as Level
            val boneMealStack = ItemStack(Items.BONE_MEAL)

            val minX = floor(center.x - radius).toInt()
            val maxX = ceil(center.x + radius).toInt()
            val minY = floor(center.y - radius).toInt()
            val maxY = ceil(center.y + radius).toInt()
            val minZ = floor(center.z - radius).toInt()
            val maxZ = ceil(center.z + radius).toInt()

            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    for (z in minZ..maxZ) {
                        val pos = BlockPos(x, y, z)
                        val blockCenter = Vec3.atCenterOf(pos)
                        if (blockCenter.distanceTo(center) <= radius && !world.isEmptyBlock(pos)) {
                            // Use Mojang-mapped API: applyBonemeal needs a Player—
                            // if you don't have one, use growCrop instead:
                            BoneMealItem.growCrop(boneMealStack, world, pos)
                        }
                    }
                }
            }
        }
    }
}
