package net.abit.abitmorehex.mixin;

import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
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
     * @reason Use the player's current pigment's ColorProvider instead of the static enum colour.
     */
    @Overwrite(remap = false)
    public final int getColor() {
        var mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            // Get the FrozenPigment for this player
            var pigment = IXplatAbstractions.INSTANCE.getPigment(mc.player);
            // Each pigment has a ColorProvider you can sample
            var provider = pigment.getColorProvider();
            // sample at current world time & player position
            long worldTime = mc.level.getGameTime();
            Vec3 pos = mc.player.position();
            return provider.getColor((float)worldTime, pos);
        }
        // fallback to the enum's baked‑in colour
        return this.color;
    }

    /**
     * @reason Likewise for fade‑colour
     */
    @Overwrite(remap = false)
    public final int getFadeColor() {
        var mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            var pigment = IXplatAbstractions.INSTANCE.getPigment(mc.player);
            var provider = pigment.getColorProvider();
            long worldTime = mc.level.getGameTime();
            Vec3 pos = mc.player.position();
            // maybe sample a tick later or same, up to you
            return provider.getColor((float)(worldTime + 1), pos);
        }
        return this.fadeColor;
    }
}
