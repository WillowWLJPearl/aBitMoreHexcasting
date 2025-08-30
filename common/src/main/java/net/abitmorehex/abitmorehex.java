package net.abitmorehex;

import net.abitmorehex.registry.abitmorehexBlockRegistry;
import net.abitmorehex.registry.abitmorehexIotaTypeRegistry;
import net.abitmorehex.registry.abitmorehexItemRegistry;
import net.abitmorehex.registry.abitmorehexActionRegistry;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class abitmorehex {
    public static final String MOD_ID = "abitmorehex";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static void init() {
        LOGGER.info("aBitMore hexes is aBit and Loaded!");

        // If you have other registries, init them as well
        abitmorehexItemRegistry.init();
        abitmorehexBlockRegistry.init();
        abitmorehexIotaTypeRegistry.init();

        // This is the important part: register your actions/patterns
        abitmorehexActionRegistry.init();

        LOGGER.info("Config path: " + DummyAbstractions.getConfigDirectory()
                .toAbsolutePath().normalize());
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }
}
