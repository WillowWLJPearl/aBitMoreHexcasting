package net.abit.abitmorehex.mixin;

import at.petrak.hexcasting.api.casting.eval.ExecutionClientView;
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.client.gui.GuiSpellcasting;
import dev.architectury.platform.Platform;
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
        if (!Platform.isForge()) return info.getResolutionType();

        var mc = Minecraft.getInstance();
        if (mc.player == null) return info.getResolutionType();

        try {
            return AbitmorehexClient.INSTANCE.getTypeFor(info.getResolutionType());
        } catch (Throwable t) {
            return info.getResolutionType();
        }
    }

}
