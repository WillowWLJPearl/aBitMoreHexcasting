// src/commonMain/kotlin/net/yourmod/Registry.kt
package net.abit.abitmorehex.registry

import net.abit.abitmorehex.effects.GraspingVines
import net.abit.abitmorehex.effects.LeechingVines
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.effect.MobEffect

object AbitmorehexEffects
    
    : AbitmorehexRegistrar<MobEffect>(
    Registries.MOB_EFFECT,         // your custom effect registry key
    { BuiltInRegistries.MOB_EFFECT }           // supplier for the registry instance
) {
    val GRASPING_VINES: AbitmorehexRegistrar<MobEffect>.Entry<GraspingVines> = register("grasping_vines") { GraspingVines() }
    val LEECHING_VINES: AbitmorehexRegistrar<MobEffect>.Entry<LeechingVines> = register("leeching_vines") { LeechingVines() }


}
