package net.abit.abitmorehex.registry.item

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.item.MediaHolderItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.api.utils.MathUtils.clamp
import at.petrak.hexcasting.common.items.magic.ItemMediaHolder
import at.petrak.hexcasting.common.items.storage.ItemFocus
import net.abit.abitmorehex.registry.eval.CircletCastingEnvironment
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level
import org.apache.logging.log4j.LogManager
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.function.UnaryOperator


class AkashicWoodCirclet(material: ArmorMaterial, type: Type, properties: Properties) : ArmorItem(material, type,
    properties
), IotaHolderItem, MediaHolderItem {

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
    override fun isBarVisible(pStack: ItemStack): Boolean {
        return getMaxMedia(pStack) > 0
    }

    override fun getBarColor(pStack: ItemStack): Int {
        val media = getMedia(pStack)
        val maxMedia = getMaxMedia(pStack)
        return mediaBarColor(media, maxMedia)
    }
    override fun canProvideMedia(stack: ItemStack?): Boolean {
        return true
    }

    override fun canRecharge(stack: ItemStack?): Boolean {
        return true
    }

    override fun readIotaTag(stack: ItemStack): CompoundTag? {
        return stack.getCompound(ItemFocus.TAG_DATA)
    }


    override fun writeable(stack: ItemStack?): Boolean {
        return true
    }
    override fun canWrite(
        stack: ItemStack?,
        iota: Iota?
    ): Boolean {
        return true
    }

    override fun writeDatum(stack: ItemStack, datum: Iota?) {
        if (datum == null) {
            stack.removeTagKey(ItemFocus.TAG_DATA)
            stack.removeTagKey(ItemFocus.TAG_SEALED)
        } else if (!ItemFocus.isSealed(stack)) {
            stack.putTag(ItemFocus.TAG_DATA, IotaType.serialize(datum))
        }
    }

    override fun appendHoverText(
        stack: ItemStack,
        world: Level?,
        tooltip: MutableList<Component>,
        flag: TooltipFlag
    ) {
        // ---- media “dust” info at top ----
        val maxMedia = getMaxMedia(stack)
        if (maxMedia > 0) {
            val media    = getMedia(stack)
            val fullness = getMediaFullness(stack)
            val color    = TextColor.fromRgb(mediaBarColor(media, maxMedia))

            val dustAmt = Component.literal(
                AkashicWoodCirclet.DUST_AMOUNT.format((media  / MediaConstants.DUST_UNIT.toFloat()).toDouble())
            ).withStyle { it.withColor(ItemMediaHolder.HEX_COLOR) }

            val pct = Component.literal(
                AkashicWoodCirclet.PERCENTAGE.format((100f * fullness).toDouble()) + "%"
            ).withStyle { it.withColor(color) }

            val cap = Component.translatable(
                "hexcasting.tooltip.media",
                AkashicWoodCirclet.DUST_AMOUNT.format((maxMedia / MediaConstants.DUST_UNIT.toFloat()).toDouble())
            ).withStyle { it.withColor(ItemMediaHolder.HEX_COLOR) }

            tooltip.add(
                Component.translatable(
                    "hexcasting.tooltip.media_amount.advanced",
                    dustAmt, cap, pct
                )
            )
        }

        // ---- then the default “pattern” info below ----
        IotaHolderItem.appendHoverText(this, stack, tooltip, flag)
    }

    companion object {
        // A logger named after this class
        private val LOGGER = LogManager.getLogger()

        val PERCENTAGE: DecimalFormat = DecimalFormat("####").apply {
            roundingMode = RoundingMode.DOWN
        }

        // “Dust amount” format with grouping
        val DUST_AMOUNT: DecimalFormat = DecimalFormat("###,###.##")
    }

    override fun inventoryTick(stack: ItemStack, level: Level, entity: Entity, slotId: Int, isSelected: Boolean) {
        super.inventoryTick(stack, level, entity, slotId, isSelected)
        if (!level.isClientSide && entity is LivingEntity) {
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
    fun readIotaList(stack: ItemStack, world: ServerLevel): List<Iota> {
        // 1) Grab the root tag
        val root = stack.tag ?: return emptyList()

        // 2) Pull out the "data" compound
        if (!root.contains("data", /*TAG_Compound=*/10)) return emptyList()
        val dataTag = root.getCompound("data")

        // 3) Make sure it's actually a list‐iota
        if (dataTag.getString("hexcasting:type") != "hexcasting:list") return emptyList()

        // 4) Grab the raw ListTag under "hexcasting:data"
        val rawList = dataTag.getList("hexcasting:data", /*TAG_Compound=*/10) as? ListTag
            ?: return emptyList()

        // 5) Deserialize each element
        return rawList.mapNotNull { element ->
            // element should be a CompoundTag
            val eltTag = element as? CompoundTag ?: return@mapNotNull null
            IotaType.deserialize(eltTag, world)
        }
    }
}