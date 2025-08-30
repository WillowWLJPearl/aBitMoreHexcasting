package net.abitmorehex.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.abitmorehex.abitmorehex;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class abitmorehexItemRegistry {
    // Create a RegistryKey for the ITEM registry
    private static final RegistryKey<Registry<Object>> ITEM_REGISTRY_KEY = RegistryKey.ofRegistry(new Identifier("minecraft", "item"));

    // Deferred register for items
    public static final DeferredRegister<Object> ITEMS = DeferredRegister.create(abitmorehex.MOD_ID, ITEM_REGISTRY_KEY);

    // Example item
    public static final RegistrySupplier<Item> TEST_ITEM = ITEMS.register("test_item",
            () -> new Item(new Item.Settings())
    );

    // Creative tab
    public static final RegistrySupplier<ItemGroup> ABITMOREHEX_GROUP = (RegistrySupplier<ItemGroup>) CreativeTabRegistry.create(
            (Text) new Identifier(abitmorehex.MOD_ID, "abitmorehexing_group"),
            () -> new ItemStack(TEST_ITEM.get())
    );

    public static void init() {
        // Ensure all items are registered
        ITEMS.register();
    }
}
