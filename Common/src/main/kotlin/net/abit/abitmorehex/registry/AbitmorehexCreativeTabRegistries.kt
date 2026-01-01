package net.abit.abitmorehex.registry

import at.petrak.hexcasting.api.utils.getTag
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.api.utils.putTag
import com.ibm.icu.impl.Row
import dev.architectury.registry.CreativeTabRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.StringTag
import net.minecraft.network.chat.Component
import net.minecraft.world.flag.FeatureFlag
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import java.awt.List

object AbitmorehexCreativeTabRegistries :
    AbitmorehexRegistrar<CreativeModeTab>(
    Registries.CREATIVE_MODE_TAB ,
    { BuiltInRegistries.CREATIVE_MODE_TAB }
) {


    val ABITMOREHEXGROUP = register("abitmorehexgroup") {
        CreativeModeTab.builder(CreativeModeTab.Row.TOP, 3)
            .icon({ ItemStack(AbitmoreItems.MIKU_STAFF.value)})
            .title(Component.literal("A Bit More Hex"))
            .displayItems { parameters, output ->
                AbitmoreItems.entries.filter { if(it.equals(AbitmoreItems.SHIFTING_MEDIA)) false else true }
                .forEach { output.accept(it.value) }

                val stack = ItemStack(AbitmoreItems.SHIFTING_MEDIA.value)
                val list = ListTag()
                list.add(StringTag.valueOf("hexcasting:focus"))
               stack.tag = CompoundTag().apply { putList("mimics", list) }
                output.accept(stack)
            }
            .build()
    }
}