package net.abit.abitmorehex.mixin;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexActions;
import net.abit.abitmorehex.casting.actions.rw.armor.OpWriteHeels;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(HexActions.class)
public class MixinHexActions {
    // Shadow the private backing map of all actions
    @Shadow @Final @Mutable
    private static Map<ResourceLocation, ActionRegistryEntry> ACTIONS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void replaceWriteHeels(CallbackInfo ci) {
        // 1) Compute the exact same ID used by the vanilla entry
        ResourceLocation id = HexAPI.modLoc("append");

        // 2) Remove the old entry
        ACTIONS.remove(id);

        // 3) Build your replacement
        HexPattern   pat   = HexPattern.fromAngles("edqde", HexDir.SOUTH_WEST);
        Action        act   = OpWriteHeels.INSTANCE;   // Kotlin `object` singleton
        ActionRegistryEntry custom = new ActionRegistryEntry(pat, act);

        // 4) Put it back under the same key
        ACTIONS.put(id, custom);
    }
}
