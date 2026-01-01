package net.abit.abitmorehex.registry.eval

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.abit.abitmorehex.blockentities.RootedTable
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate


/**
 * A CastingEnvironment that casts from a single ItemStack (your circlet),
 * with no “hand” involved.
 */
class AltarCastingEnvironment(
    world: ServerLevel,
    private val vec3: Vec3,
    private val iota: List<Iota>,
    private val iotaHolderStack: ItemStack,
) : CastingEnvironment(world) {

    override fun getCastingEntity(): Nothing? = null
    override fun getMishapEnvironment(): MishapEnvironment {
        return AltarMishapEnv(world, this.caster)
    }

    public fun getAlterPos(): Vec3 {
        return vec3
    }

    override fun mishapSprayPos(): Vec3 {
        return vec3
    }

    override fun getCastingHand(): InteractionHand? = null

    override fun getUsableStacks(mode: StackDiscoveryMode): List<ItemStack> =
        listOf(iotaHolderStack)

    override fun getPrimaryStacks(): List<HeldItemInfo> =
        listOf(HeldItemInfo(iotaHolderStack, null))

    override fun replaceItem(
        stackOk: Predicate<ItemStack?>?,
        replaceWith: ItemStack?,
        hand: InteractionHand?
    ): Boolean {
        return false
    }

    override fun getPigment(): FrozenPigment? {
        return HexAPI.instance().getColorizer(this.caster)
    }


    override fun setPigment( pigment: FrozenPigment?): FrozenPigment? {
        return IXplatAbstractions.INSTANCE.setPigment(caster, pigment)
    }

    override fun produceParticles(particles: ParticleSpray, pigment: FrozenPigment) {
        particles.sprayParticles(this.world, pigment)
    }
    override fun printMessage(message: Component?) {
        if (message != null) {
            // on the server, send it as a system/chat message
            getAltar()?.setMishap(message)

        }
    }

    override fun postExecution(result: CastResult) {
        super.postExecution(result)
        for (side in result.sideEffects) {
            if (side is OperatorSideEffect.DoMishap) {
                val m = side.mishap
                val ctx = side.errorCtx
                val msg = m.errorMessageWithName(this, ctx)
                if (msg != null) {
                    // send directly to your player
                    getAltar()?.setMishap(msg)
                }
            }
        }
    }


    override fun hasEditPermissionsAtEnvironment(pos: BlockPos): Boolean = true

    override fun isVecInRangeEnvironment(vec: Vec3): Boolean = true

    companion object{

    }

    private fun getAltar(): RootedTable? {
        // use containing() so we floor correctly
        val pos = BlockPos.containing(vec3.x, vec3.y, vec3.z)

        // debug: let me know if we're missing it
        val be = world.getBlockEntity(pos)
        if (be !is RootedTable) {
            return null
        }

        return be
    }




    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long {
        // how much we still owe
        var owed = cost

        // grab the table BE (or bail out immediately, still owe full cost)
        val table = getAltar() ?: return owed

        // how much the table currently holds
        val available = table.getMedia()
        if (available <= 0L) return owed

        // take up to what we owe
        val toTake = minOf(owed, available)
        owed -= toTake

        // if this is a real extract (not simulation), apply it
        if (!simulate) {
            table.setMedia(available - toTake)
            table.setChanged()                                  // mark dirty
            // sync to clients
            if (world is ServerLevel) {
                world.sendBlockUpdated(
                    table.blockPos,
                    table.blockState,
                    table.blockState,
                    3 /* UPDATE flags: clients + comparator */
                )
            }
        }

        return owed
    }







    fun getAllowlist(): Set<ResourceLocation> = emptySet()

    fun allowLateExtraction(): Boolean = false

    // …and any other abstract methods your version of the API requires…
}
