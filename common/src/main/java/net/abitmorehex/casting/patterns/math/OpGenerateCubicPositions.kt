package net.abitmorehex.casting.patterns.math

import at.petrak.hexcasting.api.spell.ConstMediaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.*
import net.minecraft.util.math.Vec3d

class OpGenerateCubicPositions : ConstMediaAction {
        override val argc = 2

        override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
                val origin = (args[0] as Vec3Iota).vec3
                val radius = (args[1] as DoubleIota).double.toInt()

                val positions = mutableListOf<Iota>()

                // Iterate over the cubic space around the origin
                for (x in -radius..radius) {
                        for (y in -radius..radius) {
                                for (z in -radius..radius) {
                                        val pos = origin.add(Vec3d(x.toDouble(), y.toDouble(), z.toDouble()))
                                        positions.add(Vec3Iota(pos))
                                }
                        }
                }

                return listOf(ListIota(positions))
        }
}
