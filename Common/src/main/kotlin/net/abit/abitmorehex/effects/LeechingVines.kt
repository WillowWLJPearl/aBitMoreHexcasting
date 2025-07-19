package net.abit.abitmorehex.effects

import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.api.utils.MathUtils.clamp
import net.abit.abitmorehex.registry.AbitmoreItems
import net.abit.abitmorehex.registry.item.AkashicWoodCirclet
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSources
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.item.ItemStack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class LeechingVines : MobEffect(
    MobEffectCategory.HARMFUL,
    0x98FB98
) {
    // Run every single tick:
    override fun isDurationEffectTick(duration: Int, amplifier: Int): Boolean = true
    override fun applyEffectTick(entity: LivingEntity, amplifier: Int) {
        // Only run on the server
        if (entity.level().isClientSide) return
        entity.hurt(entity.damageSources().magic(), 0.2f + amplifier)
        // Define how far out you want to check (5 blocks here)
        val radius = 5.0
        val world  = entity.level() as ServerLevel

        // Collect *all* living entities within an axis-aligned box
        val nearby = world.getEntitiesOfClass(
            LivingEntity::class.java,
            entity.boundingBox.inflate(radius),
            { true }  // no extra filter; you can exclude villagers, etc. here
        )

        for (target in nearby) {

            // —— then tag their equipment stacks if they’re wearing your circlet ——
            for (slot in EquipmentSlot.values()) {
                if (slot.type == EquipmentSlot.Type.ARMOR) {
                    val stack = target.getItemBySlot(slot)
                    if (!stack.isEmpty && stack.item is AkashicWoodCirclet) {
                       val mediaToHealthRate = HexConfig.common().mediaToHealthRate()
                        val tag   = stack.orCreateTag
                        tag.putInt("hexcasting:media",
                            clamp((
                                    (tag.getLong("hexcasting:media") + ((0.2 + amplifier) * mediaToHealthRate)/3).toLong()),
                                0,
                                tag.getLong("hexcasting:start_media")
                            )
                                .toInt()
                        )
                    }
                }
            }
        }
    }


    override fun addAttributeModifiers(
        livingEntity: LivingEntity,
        attributeMap: AttributeMap,
        amplifier: Int
    ) {
        // Always call super first so any built-in modifiers apply:
        super.addAttributeModifiers(livingEntity, attributeMap, amplifier)

        // Apply –15% walk speed per amplifier level:
        val level = amplifier + 1
        val slowdownAmount = -0.05 * level
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            UUID.fromString("e28d32f9-1e74-4e86-9b76-9a6b9f1f7271")
                .toString(),
            slowdownAmount,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        )
    }
}
