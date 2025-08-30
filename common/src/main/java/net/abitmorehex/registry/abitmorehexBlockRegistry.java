package net.abitmorehex.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.abitmorehex.abitmorehex;
import net.abitmorehex.blocks.EdifiedSignBlock;
import net.abitmorehex.blocks.tileentities.EdifiedSignBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

public class abitmorehexBlockRegistry {
    // Registry keys for block and block entity registries
    private static final RegistryKey<Registry<Object>> BLOCK_REGISTRY_KEY = RegistryKey.ofRegistry(new Identifier("minecraft", "block"));
    private static final RegistryKey<Registry<Object>> BLOCK_ENTITY_REGISTRY_KEY = RegistryKey.ofRegistry(new Identifier("minecraft", "block_entity_type"));

    // Deferred registers
    public static final DeferredRegister<Object> BLOCKS = DeferredRegister.create(abitmorehex.MOD_ID, BLOCK_REGISTRY_KEY);
    public static final DeferredRegister<Object> BLOCK_ENTITIES = DeferredRegister.create(abitmorehex.MOD_ID, BLOCK_ENTITY_REGISTRY_KEY);

    // Example block
    public static final RegistrySupplier<Block> EDIFIED_SIGN_BLOCK = BLOCKS.register("edified_sign",
            () -> new EdifiedSignBlock(Block.Settings.copy(Blocks.OAK_SIGN))
    );

    // Example block entity
    public static final RegistrySupplier<BlockEntityType<EdifiedSignBlockEntity>> EDIFIED_SIGN_BLOCK_ENTITY = BLOCK_ENTITIES.register("edified_sign",
            () -> BlockEntityType.Builder.create(EdifiedSignBlockEntity::new, EDIFIED_SIGN_BLOCK.get()).build(null)
    );

    public static void init() {
        BLOCKS.register();
        BLOCK_ENTITIES.register();
    }
}
