package net.abit.abitmorehex.casting.actions.altars

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import net.abit.abitmorehex.registry.eval.AltarCastingEnvironment
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment

object OpContainerPos : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        if (env is AltarCastingEnvironment)
            return env.getAlterPos().asActionResult
        if(env is SubCastingEnvironment)
            return env.getContainerVec3().asActionResult
            else
            throw MishapNoSpellCircle()


    }
}
