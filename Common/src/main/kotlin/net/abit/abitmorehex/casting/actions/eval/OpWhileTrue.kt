package net.abit.abitmorehex.casting.actions.eval

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.evaluatable
import at.petrak.hexcasting.api.casting.iota.BooleanIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.item.ItemStack

object OpWhileTrue : Action {
    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        // 1) Pull the list to loop over off of the end of the *real* image stack
        val base = image.stack.toMutableList()
        val listIota = base.removeLastOrNull() as? ListIota
            ?: throw MishapNotEnoughArgs(1, image.stack.size)

        // 2) Clone *all* of the original image's state, but start with an empty stack
        var vmImage = image.copy(stack = emptyList())
        val vm = CastingVM(image = vmImage, env = env)

        // 3) Run your while–true loop *in that same context*
        while (true) {
            // queue up exactly the pattern you want to run each iteration
            vm.queueExecuteAndWrapIotas(listIota.list.toList(), world = env.world)

            // check the *same* vm.image.stack for your BooleanIota
            val top = vm.image.stack.lastOrNull()
            if (top is BooleanIota && top.bool) {
                // consume that Boolean, but *keep* every write/local that happened
                vm.image = vm.image.copy(stack = vm.image.stack.dropLast(1))
            } else {
                break
            }
        }

        // 4) Merge what’s left on the VM’s stack back into your original image’s stack
        val merged = base + vm.image.stack
        val resultImage = image.withUsedOp().copy(stack = merged)

        return OperationResult(
            resultImage,
            emptyList(),
            continuation,
            HexEvalSounds.NORMAL_EXECUTE
        )
    }
}
