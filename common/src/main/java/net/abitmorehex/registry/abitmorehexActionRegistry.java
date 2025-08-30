package net.abitmorehex.registry;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.casting.castables.Action;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.HexRegistries;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.abitmorehex.abitmorehex;
import net.abitmorehex.casting.patterns.math.*;
import net.abitmorehex.casting.patterns.spells.OpCreeperFireworkSpell;
import net.minecraft.util.Identifier;

/**
 * Example registry for "patterns" (now ActionRegistryEntries),
 * using the new Hexcasting approach.
 */
public class abitmorehexActionRegistry {
    // 1) Use HexRegistries.ACTION as the ResourceKey<Registry<ActionRegistryEntry>>
    public static final DeferredRegister<ActionRegistryEntry> ACTIONS =
            DeferredRegister.create(abitmorehex.MOD_ID, HexRegistries.ACTION);

    // 2) Register each ActionRegistryEntry using (HexPattern, Action).
    //    The name in register("creeperhiss", ...) is used to build the ResourceLocation internally.
    public static final RegistrySupplier<ActionRegistryEntry> CREEPERHISS =
            ACTIONS.register("creeperhiss",
                    () -> new ActionRegistryEntry(
                            // HexPattern: used to detect the spell in-world
                            HexPattern.fromAngles("aqde", HexDir.EAST),
                            // The Action: your custom logic
                            new OpCreeperFireworkSpell()
                    )
            );

    public static final RegistrySupplier<ActionRegistryEntry> THOUGHTCLEAR =
            ACTIONS.register("thoughtclear",
                    () -> new ActionRegistryEntry(
                            HexPattern.fromAngles("adadadeaqqq", HexDir.NORTH_WEST),
                            new OpRemoveEveryNth()
                    )
            );

    public static final RegistrySupplier<ActionRegistryEntry> THOUGHTCLUTTER =
            ACTIONS.register("thoughtclutter",
                    () -> new ActionRegistryEntry(
                            HexPattern.fromAngles("dadadawedqdew", HexDir.NORTH_EAST),
                            new OpReplaceEveryNth()
                    )
            );

    public static final RegistrySupplier<ActionRegistryEntry> UNVEILEDSIGHTS =
            ACTIONS.register("unveiledsights",
                    () -> new ActionRegistryEntry(
                            HexPattern.fromAngles("wdaqqqaqeqaeaqa", HexDir.EAST),
                            new OpBlockRaycastContinue()
                    )
            );

    public static final RegistrySupplier<ActionRegistryEntry> ROUNDABOUT =
            ACTIONS.register("roundaboutcubic",
                    () -> new ActionRegistryEntry(
                            HexPattern.fromAngles("wwdaqqqaqw", HexDir.EAST),
                            new OpGenerateCubicPositions()
                    )
            );

    public static final RegistrySupplier<ActionRegistryEntry> COMPAREPOSTOBLOCKS =
            ACTIONS.register("compareblocks",
                    () -> new ActionRegistryEntry(
                            HexPattern.fromAngles("wwdaqqqa", HexDir.EAST),
                            new OpCompareBlocks()
                    )
            );

    public static final RegistrySupplier<ActionRegistryEntry> RANDOMIZELIST =
            ACTIONS.register("randomizelist",
                    () -> new ActionRegistryEntry(
                            HexPattern.fromAngles("adwqqqqae", HexDir.NORTH_WEST),
                            new OpRandomizeList()
                    )
            );

    // 3) Architectury: call ACTIONS.register() in an init() method,
    //    so everything is actually submitted to the registry.
    public static void init() {
        ACTIONS.register();
    }
}
