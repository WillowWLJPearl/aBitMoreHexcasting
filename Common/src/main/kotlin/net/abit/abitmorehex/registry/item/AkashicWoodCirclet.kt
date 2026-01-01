package net.abit.abitmorehex.registry.item

import at.petrak.hexcasting.annotations.SoftImplement
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.item.MediaHolderItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.api.utils.MathUtils.clamp
import at.petrak.hexcasting.common.items.storage.ItemFocus
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import net.abit.abitmorehex.registry.eval.CircletCastingEnvironment
import net.minecraft.core.BlockSource
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DispenserBlock
import org.apache.logging.log4j.LogManager
import java.math.RoundingMode
import java.text.DecimalFormat

// If Hex exposes this in your env (same interface Lens implements):
interface HexBaubleItem

class AkashicWoodCirclet(
    properties: Properties
) : Item(properties), HexBaubleItem, IotaHolderItem, MediaHolderItem {

    init {
        // Allow armor-dispenser auto-equip to head, like ItemLens does.
        DispenserBlock.registerBehavior(this, object : OptionalDispenseItemBehavior() {
            override fun execute(world: BlockSource, stack: ItemStack): ItemStack {
                // Reuse vanilla armor equipping helper
                this.setSuccess(net.minecraft.world.item.ArmorItem.dispenseArmor(world, stack))
                return stack
            }
        })
    }

    // ---------- MediaHolderItem ----------
    override fun getMedia(stack: ItemStack): Long {
        if (stack.hasInt(ItemMediaHolder.TAG_MEDIA)) return stack.getInt(ItemMediaHolder.TAG_MEDIA).toLong()
        return stack.getLong(ItemMediaHolder.TAG_MEDIA)
    }

    override fun getMaxMedia(stack: ItemStack): Long {
        if (stack.hasInt(ItemMediaHolder.TAG_MAX_MEDIA)) return stack.getInt(ItemMediaHolder.TAG_MAX_MEDIA).toLong()
        return stack.getLong(ItemMediaHolder.TAG_MAX_MEDIA)
    }

    override fun setMedia(stack: ItemStack, media: Long) {
        stack.putLong(ItemMediaHolder.TAG_MEDIA, clamp(media, 0, getMaxMedia(stack)))
    }

    fun setMaxMedia(stack: ItemStack, media: Long) {
        stack.putLong(ItemMediaHolder.TAG_MAX_MEDIA, media)
    }

    override fun canProvideMedia(stack: ItemStack?): Boolean = false
    override fun canRecharge(stack: ItemStack?): Boolean = true

    // Show Hex-style media bar on items
    override fun isBarVisible(stack: ItemStack): Boolean = getMaxMedia(stack) > 0

    override fun getBarWidth(stack: ItemStack): Int {
        val fullness = getMediaFullness(stack)
        return (13.0f * fullness).toInt()
    }

    override fun getBarColor(stack: ItemStack): Int {
        val media = getMedia(stack)
        val maxMedia = getMaxMedia(stack)
        return mediaBarColor(media, maxMedia)
    }

    // ---------- IotaHolderItem ----------
    override fun readIotaTag(stack: ItemStack): CompoundTag? = stack.getCompound(ItemFocus.TAG_DATA)

    override fun writeable(stack: ItemStack?): Boolean = true

    override fun canWrite(stack: ItemStack?, iota: Iota?): Boolean = true

    override fun writeDatum(stack: ItemStack, datum: Iota?) {
        if (datum == null) {
            stack.removeTagKey(ItemFocus.TAG_DATA)
            stack.removeTagKey(ItemFocus.TAG_SEALED)
        } else if (!ItemFocus.isSealed(stack)) {
            stack.putTag(ItemFocus.TAG_DATA, IotaType.serialize(datum))
        }
    }

    // ---------- Tooltip ----------
    override fun appendHoverText(
        stack: ItemStack,
        world: Level?,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        val maxMedia = getMaxMedia(stack)
        if (maxMedia > 0) {
            val media = getMedia(stack)
            val fullness = getMediaFullness(stack)
            val color = TextColor.fromRgb(mediaBarColor(media, maxMedia))

            val dustAmt = Component.literal(
                DUST_AMOUNT.format((media / MediaConstants.DUST_UNIT.toFloat()).toDouble())
            ).withStyle { it.withColor(ItemMediaHolder.HEX_COLOR) }

            val pct = Component.literal(
                PERCENTAGE.format((100f * fullness).toDouble()) + "%"
            ).withStyle { it.withColor(color) }

            val cap = Component.translatable(
                "hexcasting.tooltip.media",
                DUST_AMOUNT.format((maxMedia / MediaConstants.DUST_UNIT.toFloat()).toDouble())
            ).withStyle { it.withColor(ItemMediaHolder.HEX_COLOR) }

            tooltip.add(
                Component.translatable("hexcasting.tooltip.media_amount.advanced", dustAmt, cap, pct)
            )
        }

        IotaHolderItem.appendHoverText(this, stack, tooltip, flag)
    }

    // ---------- Equip slot hint (Forge) ----------
    @SoftImplement("forge")
    fun getEquipmentSlot(stack: ItemStack): EquipmentSlot = EquipmentSlot.HEAD

    // ---------- Wearer logic (same as before) ----------
    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        super.inventoryTick(stack, level, entity, slotId, isSelected)
        if (!level.isClientSide && entity is LivingEntity) {
            val headStack = entity.getItemBySlot(EquipmentSlot.HEAD)
            if (headStack !== stack) return
            setMaxMedia(stack, getMaxMedia(stack)+1)
            val living = entity as LivingEntity
            val tag    = stack.orCreateTag
            val wasHurt = tag.getBoolean("AWH_PreviouslyHurt")

            // 2) Are they hurt *right now*?
            val isHurt  = living.hurtTime > 0
            // hurtTime is set to maxHurtTime when the entity is damaged

            if (isHurt && !wasHurt) {
                // → the wearer just got hit
                //  do your effect here, e.g.:

                val iota = readIotaList(stack, level as ServerLevel)


                val image: CastingImage = CastingImage()
                tag.putBoolean("AWH_PreviouslyHurt", true)
                CastingVM(
                    image = image,
                    env = CircletCastingEnvironment(level, entity as ServerPlayer,stack)
                ).queueExecuteAndWrapIotas(
                    iotas = iota,
                    world = level
                )
            }
            else if (!isHurt && wasHurt) {
                // recovered—reset for next hit
                tag.putBoolean("AWH_PreviouslyHurt", false)
            }
        }

    }

    companion object {
        private val LOGGER = LogManager.getLogger()

        val PERCENTAGE: DecimalFormat = DecimalFormat("####").apply {
            roundingMode = RoundingMode.DOWN
        }
        val DUST_AMOUNT: DecimalFormat = DecimalFormat("###,###.##")
    }
}

// --------- helpers you already had ---------
public fun readIotaList(stack: ItemStack, world: ServerLevel): List<Iota> {
    val root = stack.tag ?: return emptyList()
    if (!root.contains("data", 10)) return emptyList()
    val dataTag = root.getCompound("data")
    if (dataTag.getString("hexcasting:type") != "hexcasting:list") return emptyList()
    val rawList = dataTag.getList("hexcasting:data", 10) as? ListTag ?: return emptyList()

    return rawList.mapNotNull { element ->
        val eltTag = element as? CompoundTag ?: return@mapNotNull null
        IotaType.deserialize(eltTag, world)
    }
}

public fun readSubIotaList(stack: ItemStack, world: ServerLevel): List<Iota> {
    val root = stack.tag ?: return emptyList()
    if (!root.contains("bdata", 10)) return emptyList()
    val dataTag = root.getCompound("bdata")
    if (dataTag.getString("hexcasting:type") != "hexcasting:list") return emptyList()
    val rawList = dataTag.getList("hexcasting:data", 10) as? ListTag ?: return emptyList()

    return rawList.mapNotNull { element ->
        val eltTag = element as? CompoundTag ?: return@mapNotNull null
        IotaType.deserialize(eltTag, world)
    }
}
