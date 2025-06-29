package net.abit.abitmorehex.effects

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.damagesource.DamageSources
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class GraspingVines : MobEffect(
    MobEffectCategory.HARMFUL,
    0x98FB98
) {
    // Run every single tick:
    override fun isDurationEffectTick(duration: Int, amplifier: Int): Boolean = true
    companion object {
        private val lastPos = ConcurrentHashMap<UUID, Pair<Double, Double>>()
    }
    override fun applyEffectTick(entity: LivingEntity, amplifier: Int) {
        // Only on server
        if (entity.level().isClientSide) return

        // Compute and store position-based movement
        val id = entity.uuid
        val (oldX, oldZ) = lastPos.computeIfAbsent(id) { entity.x to entity.z }
        val dx = abs(entity.x - oldX)
        val dz = abs(entity.z - oldZ)

        // If they moved
        if (dx != 0.0 || dz != 0.0) {
            entity.hurt(entity.damageSources().magic(), 1.0f + amplifier)
        }

        // Update for next tick
        lastPos[id] = entity.x to entity.z
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
        val slowdownAmount = -0.6 * level
        this.addAttributeModifier(
            Attributes.MOVEMENT_SPEED,
            UUID.fromString("e28d32f9-1e74-4e86-9b76-9a6b9f1f7271")
                .toString(),
            slowdownAmount,
            AttributeModifier.Operation.MULTIPLY_TOTAL
        )
    }
}
