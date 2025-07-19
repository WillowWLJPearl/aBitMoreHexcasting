package net.abit.abitmorehex.registry.eval

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.MishapEnvironment
import at.petrak.hexcasting.api.casting.eval.sideeffects.OperatorSideEffect
import at.petrak.hexcasting.api.item.MediaHolderItem
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.Container
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.function.Predicate
import kotlin.math.floor


/**
 * A CastingEnvironment that casts from a single ItemStack (your circlet),
 * with no “hand” involved.
 */
class SubCastingEnvironment(
    world: ServerLevel,
    private val vec3: Vec3,
    private val slot: Int,
    private val iotaHolderStack: ItemStack,
) : CastingEnvironment(world) {

    override fun getCastingEntity() = getContainerPlayer()
    override fun getMishapEnvironment(): MishapEnvironment {
        return AltarMishapEnv(world, this.caster)
    }

    public fun getContainerVec3(): Vec3 {
        return Vec3(floor(vec3.x),floor(vec3.y),floor(vec3.z))
    }
    /**
     * Find the nearest player within 1 block of the Vec3, or null if none.
     */
    private fun getContainerPlayer(): Player? =
        world.getNearestPlayer(vec3.x, vec3.y, vec3.z, /* horizontal maxDist */ 1.0, { true })

    fun getContainer() : Container {
        val pos = BlockPos.containing(vec3.x, vec3.y, vec3.z)
       val be =  world.getBlockEntity(pos)
        return if(be is Container) {
            be
        } else {
            castingEntity?.inventory as Container
        }
    }

    public fun getIotaHolder() : ItemStack {
        return iotaHolderStack
    }

    override fun mishapSprayPos(): Vec3 {
        return this.vec3
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
        return if(this.caster != null)
            HexAPI.instance().getColorizer(this.caster)
        else
            FrozenPigment.DEFAULT.get()
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
            getContainerPlayer()?.sendSystemMessage(message)

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
                    getContainerPlayer()?.sendSystemMessage(msg)
                }
            }
        }
    }


    override fun hasEditPermissionsAtEnvironment(pos: BlockPos): Boolean = true

    override fun isVecInRangeEnvironment(vec: Vec3): Boolean = true

    companion object{

    }




    override fun extractMediaEnvironment(cost: Long, simulate: Boolean): Long {
        var owed = cost
        // 1) Ask the item to pay up to `cost`
        if(iotaHolderStack.item is MediaHolderItem) {
            val paid = (iotaHolderStack.item as? MediaHolderItem)
                ?.withdrawMedia(iotaHolderStack, cost, simulate)
                ?: 0L

            // 2) Return how much is still owed:
            //    ≤0 means fully paid; >0 means not enough media → mishap
            owed -= paid
        }
        if (owed > 0) {
            // 1) Try the block’s own inventory (e.g. chest, hopper…) at vec3
            val pos = BlockPos.containing(vec3.x, vec3.y, vec3.z)
            world.getBlockEntity(pos)?.let { be ->
                if (be is Container) {
                    for (i in 0 until be.getContainerSize()) {
                        val stackInSlot = be.getItem(i)
                        if (stackInSlot.item is MediaHolderItem) {
                            val pulled = (stackInSlot.item as MediaHolderItem)
                                .withdrawMedia(stackInSlot, owed, simulate)
                            owed -= pulled
                            if (owed <= 0L) break
                        }
                    }
                }
            }

            // 2) Fallback: find a Player at vec3 and pull from their inventory
            if (owed > 0) {
                val searchBox = AABB(vec3, vec3).inflate(0.5)
                val targetPlayer = world.getEntitiesOfClass(
                    Player::class.java,
                    searchBox,
                    { true }
                ).firstOrNull()

                if (targetPlayer != null) {
                    for (stackInSlot in targetPlayer.inventory.items) {
                        if (stackInSlot.item is MediaHolderItem) {
                            val pulled = (stackInSlot.item as MediaHolderItem)
                                .withdrawMedia(stackInSlot, owed, simulate)
                            owed -= pulled
                            if (owed <= 0L) break
                        }
                    }
                }
            }

            // 3) Done: return whatever is still owed
            return owed
        } else

        return owed
        }






    fun getAllowlist(): Set<ResourceLocation> = emptySet()

    fun allowLateExtraction(): Boolean = false

    // …and any other abstract methods your version of the API requires…
}
