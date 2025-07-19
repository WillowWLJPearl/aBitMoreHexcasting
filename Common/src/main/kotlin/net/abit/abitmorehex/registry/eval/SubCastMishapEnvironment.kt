package net.abit.abitmorehex.registry.eval

import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

class SubCastMishapEnvironment(world: ServerLevel?, caster: ServerPlayer?) : MishapEnvironment(world, caster) {
    override fun yeetHeldItemsTowards(targetPos: Vec3?) {
        return
    }

    override fun dropHeldItems() {
        return
    }

    override fun drown() {
        return
    }

    override fun damage(healthProportion: Float) {
        return
    }

    override fun removeXp(amount: Int) {
        return
    }

    override fun blind(ticks: Int) {
        return
    }
}