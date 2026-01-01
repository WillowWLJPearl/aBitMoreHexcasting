package net.abit.abitmorehex.registry.eval.mishaps

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.network.chat.Component
import net.minecraft.world.item.DyeColor

class MishapWrongSlot(
    val slot: Int,
    val bounds: List<Int>
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.PURPLE)

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        env.mishapEnvironment.drown()
    }

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Component =
         Component.literal("slot $slot out of bounds, expected slot to be within the bounds of ${bounds.last()} and ${bounds.first()}")
}