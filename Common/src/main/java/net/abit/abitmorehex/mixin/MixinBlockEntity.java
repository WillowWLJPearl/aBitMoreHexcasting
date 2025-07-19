package net.abit.abitmorehex.mixin;

import net.abit.abitmorehex.registry.item.ItemShiftingMedia;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercept every BlockEntity#setChanged() call and, if it's
 * a ChestBlockEntity on the server, run your container‐tick logic.
 */
@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity {
    @Inject(method = "setChanged*", at = @At("TAIL"))
    private void onSetChanged(CallbackInfo ci) {
        // cast back to the real BlockEntity
        BlockEntity be = (BlockEntity)(Object)this;
        Level world = be.getLevel();
        // only on the logical server
        if (!(world instanceof ServerLevel server)) return;
        // only for chests
        if (!(be instanceof ChestBlockEntity chest)) return;

        // build the Vec3 at the chest’s center
        BlockPos pos = be.getBlockPos();
        Vec3 vec3 = new Vec3(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
        );

        // for each slot’s stack, invoke your Kotlin handler
        for (int i = 0; i < ((Container) chest).getContainerSize(); i++) {
            ItemStack stack = ((Container) chest).getItem(i);
            ItemShiftingMedia.Companion.onInventoryChanged(stack, vec3, chest, server);
        }
    }
}
