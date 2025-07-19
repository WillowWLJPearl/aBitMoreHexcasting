package net.abit.abitmorehex;

import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class PatternTypeOverrides {
    private static final Map<ResolvedPatternType, Integer> colorOverrides =
            new EnumMap<>(ResolvedPatternType.class);
    private static final Map<ResolvedPatternType, Integer> fadeOverrides =
            new EnumMap<>(ResolvedPatternType.class);
    private static final Map<ResolvedPatternType, Boolean> successOverrides =
            new EnumMap<>(ResolvedPatternType.class);

    // Example: call this during your mod initialization or via commands
    public static void setColorOverride(ResolvedPatternType type, int color) {
        colorOverrides.put(type, color);
    }

    public static OptionalInt getColorOverride(ResolvedPatternType type) {
        return colorOverrides.containsKey(type)
                ? OptionalInt.of(colorOverrides.get(type))
                : OptionalInt.empty();
    }

    // Similar for fadeColor
    public static void setFadeOverride(ResolvedPatternType type, int fade) {
        fadeOverrides.put(type, fade);
    }

    public static OptionalInt getFadeOverride(ResolvedPatternType type) {
        return fadeOverrides.containsKey(type)
                ? OptionalInt.of(fadeOverrides.get(type))
                : OptionalInt.empty();
    }

    // And for success
    public static void setSuccessOverride(ResolvedPatternType type, boolean success) {
        successOverrides.put(type, success);
    }

    public static Optional<Boolean> getSuccessOverride(ResolvedPatternType type) {
        return Optional.ofNullable(successOverrides.get(type));
    }
}
