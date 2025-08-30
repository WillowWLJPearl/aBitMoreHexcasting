package net.abitmorehex.registry;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

import static net.abitmorehex.abitmorehex.id;

public class abitmorehexIotaTypeRegistry {
    public static Map<Identifier, IotaType<?>> TYPES = new HashMap<>();

    public static void init() {
        for (Map.Entry<Identifier, IotaType<?>> entry : TYPES.entrySet()) {
            Registry.register(HexIotaTypes.REGISTRY, entry.getKey(), entry.getValue());
        }
    }

    private static <U extends Iota, T extends IotaType<U>> T register(String name, T type) {
        IotaType<?> old = TYPES.put(id(name), type);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + name);
        }
        return type;
    }
}
