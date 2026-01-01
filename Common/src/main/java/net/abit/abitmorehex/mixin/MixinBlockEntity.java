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

@Mixin(net.minecraft.world.level.block.entity.BlockEntity.class)
public abstract class MixinBlockEntity {
    @Inject(method = "setChanged()V", at = @At("TAIL"), remap = false, require = 1)
    private void onSetChanged(org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        var be = (net.minecraft.world.level.block.entity.BlockEntity)(Object)this;
        var level = be.getLevel();
        if (!(level instanceof net.minecraft.server.level.ServerLevel server)) return;
        if (!(be instanceof net.minecraft.world.level.block.entity.ChestBlockEntity chest)) return;

        var pos = be.getBlockPos();
        var c = new net.minecraft.world.phys.Vec3(pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5);
        net.minecraft.world.Container inv = chest;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            net.abit.abitmorehex.registry.item.ItemShiftingMedia.Companion.onInventoryChanged(stack, c, chest, server);
        }
    }
}

