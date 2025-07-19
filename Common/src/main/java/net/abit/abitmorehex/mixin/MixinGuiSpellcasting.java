package net.abit.abitmorehex.mixin;

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import net.abit.abitmorehex.AbitmorehexClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GuiSpellcasting.class, remap = false)
public class MixinGuiSpellcasting {
    @Redirect(
            method = "recvServerUpdate(Lat/petrak/hexcasting/api/casting/eval/ExecutionClientView;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lat/petrak/hexcasting/api/casting/eval/ExecutionClientView;" +
                            "getResolutionType()Lat/petrak/hexcasting/api/casting/eval/ResolvedPatternType;"
            )
    )
    private ResolvedPatternType redirectGetResolutionType(ExecutionClientView info) {
        var mc = Minecraft.getInstance();
        // if there's no player yet, just return whatever the server sent
        if (mc.player == null) {
            return info.getResolutionType();
        }
        // call your no‑arg method on the Kotlin singleton
        ResolvedPatternType custom = AbitmorehexClient.INSTANCE.getTypeFor();
        // guard in case it ever returns null (shouldn't, but safety first)
        return (custom != null) ? custom : info.getResolutionType();
    }
}
