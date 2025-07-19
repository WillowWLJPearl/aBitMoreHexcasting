package net.abit.abitmorehex.mixin;

import at.petrak.hexcasting.common.items.pigment.ItemPridePigment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemPridePigment.Type.class)
public interface TypeAccessor {
    @Accessor("colors")
    int[] getColors();
}
