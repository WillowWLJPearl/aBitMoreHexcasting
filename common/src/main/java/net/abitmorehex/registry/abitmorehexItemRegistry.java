package net.abitmorehex.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.abitmorehex.abitmorehex;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import static net.abitmorehex.abitmorehex.id;

public class abitmorehexItemRegistry {
    // Register items through this
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(abitmorehex.MOD_ID, Registry.ITEM_KEY);

    public static void init() {
        ITEMS.register();
    }

    // A new creative tab. Notice how it is one of the few things that are not deferred
    public static final ItemGroup ABITMOREHEX_GROUP = CreativeTabRegistry.create(id("abitmorehexing_group"), () -> new ItemStack(abitmorehexItemRegistry.TEST_ITEM.get()));

    // During the loading phase, refrain from accessing suppliers' items (e.g. EXAMPLE_ITEM.get()), they will not be available
    public static final RegistrySupplier<Item> TEST_ITEM = ITEMS.register("test_item", () -> new Item(new Item.Settings().group(ABITMOREHEX_GROUP)));

}
