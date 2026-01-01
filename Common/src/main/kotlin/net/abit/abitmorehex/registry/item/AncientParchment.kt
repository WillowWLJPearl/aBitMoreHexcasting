package net.abit.abitmorehex.registry.item

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.utils.getCompound
import at.petrak.hexcasting.api.utils.putTag
import at.petrak.hexcasting.api.utils.underline
import at.petrak.hexcasting.common.items.storage.ItemFocus
import at.petrak.hexcasting.common.particles.ConjureParticleOptions
import net.abit.abitmorehex.misc.SubIotaHolderItem
import net.abit.abitmorehex.registry.eval.FreeCastingEnvironment
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.abit.abitmorehex.registry.item.ItemShiftingMedia.Companion.findSlotByReference
import net.minecraft.ChatFormatting
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AncientParchment(properties: Properties) : Item(properties), IotaHolderItem {
    companion object {
        private const val TAG_ONCE_WRITTEN = "abitmorehex_once_written"
    }

    /* ---------------- helpers ---------------- */

    private fun hasData(stack: ItemStack): Boolean =
        (stack.tag?.get(ItemFocus.TAG_DATA) as? CompoundTag)?.isEmpty == false

    private fun onceWritten(stack: ItemStack): Boolean =
        stack.tag?.getBoolean(TAG_ONCE_WRITTEN) == true

    private fun canWriteNow(stack: ItemStack?): Boolean =
        stack != null && !hasData(stack) && !onceWritten(stack)

    /* ---------------- IotaHolderItem ---------------- */

    // Must stay readable: return the stored tag if present, else null
    override fun readIotaTag(stack: ItemStack): CompoundTag? =
        stack.tag?.get(ItemFocus.TAG_DATA) as? CompoundTag

    // Display logic / APIs use this to decide if writing is allowed
    override fun writeable(stack: ItemStack?): Boolean = canWriteNow(stack)

    // Allow writing exactly once; don't allow clearing (datum == null) or overwriting
    override fun canWrite(stack: ItemStack?, iota: Iota?): Boolean =
        canWriteNow(stack) && iota != null

    override fun writeDatum(stack: ItemStack, datum: Iota?) {
        if (!canWriteNow(stack) || datum == null) return
        val tag = stack.orCreateTag
        tag.put(ItemFocus.TAG_DATA, IotaType.serialize(datum))
        tag.putBoolean(TAG_ONCE_WRITTEN, true) // latch permanently
    }
    override fun appendHoverText(
        pStack: ItemStack,
        pLevel: Level?,
        pTooltipComponents: MutableList<Component>,
        pIsAdvanced: TooltipFlag
    ) {
        IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced)
    }
    private fun spawnHexRingTowardPlayer(level: ServerLevel, player: ServerPlayer) {
        val center = player.position().add(0.0, player.bbHeight * 0.5, 0.0) // around torso
        val points = 28                 // bump up/down for density
        val radius = 1.6                // ring radius
        val pull   = 0.5             // inward speed scale
        val color  = 0x88C7FFFF.toInt() // light blue (RRGGBBAA)

        val opts = ConjureParticleOptions(color)
        val tau = (2.0 * PI)

        for (i in 0 until points) {
            val a = tau * (i.toDouble() / points)
            val sx = center.x + cos(a) * radius
            val sz = center.z + sin(a) * radius
            val sy = center.y + 0.1 * sin(a * 2) // tiny vertical wobble

            val vx = (center.x - sx) * pull
            val vy = (center.y - sy) * pull
            val vz = (center.z - sz) * pull

            // vanilla-style server spawn → auto-sent to nearby clients
            level.sendParticles(opts, sx, sy, sz, 1, vx, vy, vz, 0.0)
        }
    }

    override fun getName(stack: ItemStack): Component {
        return if (hasData(stack)) {
            Component.translatable("item.abitmorehex.ancient_parchment.written")
        } else {
            super.getName(stack)
        }
    }
    override fun use(
        level: Level,
        player: Player,
        usedHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val stack = player.getItemInHand(usedHand)

        // Only cast when we actually have stored spell data
        if (!hasData(stack)) {
            return InteractionResultHolder.pass(stack)
        }

        if (!level.isClientSide) {
            val serverLevel = level as ServerLevel
            val serverPlayer = player as ServerPlayer

            val image = CastingImage()
            val iotaList = readIotaList(stack, serverLevel)

            CastingVM(
                image = image,
                env = FreeCastingEnvironment(serverLevel, serverPlayer, stack)
            ).queueExecuteAndWrapIotas(iotas = iotaList, world = serverLevel)

            spawnHexRingTowardPlayer(serverLevel, serverPlayer)

            // Consume exactly one parchment
            stack.shrink(1)
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
    }


}