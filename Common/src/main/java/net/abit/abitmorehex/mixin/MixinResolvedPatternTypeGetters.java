package net.abit.abitmorehex.mixin;

import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ResolvedPatternType.class, remap = false)
public abstract class MixinResolvedPatternTypeGetters {
    @Shadow private int color;
    @Shadow private int fadeColor;

    /**
     * @reason Use the player's current pigment's ColorProvider instead of the static enum colour,
     *         but only on Forge.
     */
    @Overwrite(remap = false)
    public final int getColor() {
        // only on Forge, otherwise fallback immediately
        if (!Platform.isForge()) {
            return this.color;
        }

        var mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            var pigment = IXplatAbstractions.INSTANCE.getPigment(mc.player);
            var provider = pigment.getColorProvider();
            long worldTime = mc.level.getGameTime();
            Vec3 pos = mc.player.position();
            return provider.getColor((float) worldTime, pos);
        }
        return this.color;
    }

    /**
     * @reason Likewise for fade-colour, but only on Forge.
     */
    @Overwrite(remap = false)
    public final int getFadeColor() {
        if (!Platform.isForge()) {
            return this.fadeColor;
        }

        var mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            var pigment = IXplatAbstractions.INSTANCE.getPigment(mc.player);
            var provider = pigment.getColorProvider();
            long worldTime = mc.level.getGameTime();
            Vec3 pos = mc.player.position();
            return provider.getColor((float) (worldTime + 1), pos);
        }
        return this.fadeColor;
    }
}
