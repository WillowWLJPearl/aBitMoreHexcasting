package net.abit.abitmorehex.registry


import net.abit.abitmorehex.blockentities.RootedTable
import net.abit.abitmorehex.blocks.RootedTableBlock
import net.abit.abitmorehex.registry.AbitmorehexBlocks.ROOTEDTABLEBLOCK
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.entity.BlockEntityType

object AbitmorehexBlockEntities
    : AbitmorehexRegistrar<BlockEntityType<*>>(
    Registries.BLOCK_ENTITY_TYPE,
    { BuiltInRegistries.BLOCK_ENTITY_TYPE }
) {

    val ROOTED_TABLE: Entry<BlockEntityType<RootedTable>> = register("rooted_table") {
        BlockEntityType.Builder
            .of(::RootedTable, ROOTEDTABLEBLOCK.value)
            .build(null)
    }


}
