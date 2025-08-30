package net.abitmorehex.casting.patterns.math

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import net.minecraft.util.math.Vec3d

class OpGenerateCubicPositions : ConstMediaAction {
        override val argc = 2

        override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
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
