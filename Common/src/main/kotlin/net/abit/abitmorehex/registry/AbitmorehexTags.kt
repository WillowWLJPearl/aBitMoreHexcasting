package net.abit.abitmorehex.registry

import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object AbitmorehexTags {
    val SPECIAL_STAFFS: TagKey<Item> = TagKey.create(
        Registries.ITEM,
        ResourceLocation("abitmorehex", "special_staffs")
    )
}
