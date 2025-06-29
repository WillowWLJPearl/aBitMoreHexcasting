package net.abit.abitmorehex.registry.eval

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.casting.eval.env.PlayerBasedMishapEnv
import at.petrak.hexcasting.api.item.MediaHolderItem
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate


/**
 * A CastingEnvironment that casts from a single ItemStack (your circlet),
 * with no “hand” involved.
 */
class CircletCastingEnvironment(
    world: ServerLevel,
    private val player: ServerPlayer,
    private val circletStack: ItemStack
) : CastingEnvironment(world) {

    override fun getCastingEntity(): LivingEntity = player
    override fun getMishapEnvironment(): MishapEnvironment {
        return PlayerBasedMishapEnv(this.caster)
    }


    override fun mishapSprayPos(): Vec3 {
        return this.caster!!.position()
    }

    override fun getCastingHand(): InteractionHand? = null

    override fun getUsableStacks(mode: StackDiscoveryMode): List<ItemStack> =
        listOf(circletStack)

    override fun getPrimaryStacks(): List<HeldItemInfo> =
        listOf(HeldItemInfo(circletStack, null))

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
            player.sendSystemMessage(message)
        }
    }

    override fun hasEditPermissionsAtEnvironment(pos: BlockPos): Boolean = true

    override fun isVecInRangeEnvironment(vec: Vec3): Boolean = true

    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long {
        // 1) Ask the item to pay up to `cost`
        val paid = (circletStack.item as? MediaHolderItem)
            ?.withdrawMedia(circletStack, cost, simulate)
            ?: 0L

        // 2) Return how much is still owed:
        //    ≤0 means fully paid; >0 means not enough media → mishap
        return cost - paid
    }


    fun getAllowlist(): Set<ResourceLocation> = emptySet()

    fun allowLateExtraction(): Boolean = false

    // …and any other abstract methods your version of the API requires…
}
