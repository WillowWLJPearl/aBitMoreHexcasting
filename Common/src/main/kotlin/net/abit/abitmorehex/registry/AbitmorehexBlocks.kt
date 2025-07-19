package net.abit.abitmorehex.registry


import net.abit.abitmorehex.blocks.RootedTableBlock
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour


object AbitmorehexBlocks : AbitmorehexRegistrar<Block>(
    Registries.BLOCK,
    {BuiltInRegistries.BLOCK}
) {
    val ROOTEDTABLEBLOCK: AbitmorehexRegistrar<Block>.Entry<RootedTableBlock> =
        register("rooted_table_block") { RootedTableBlock(BlockBehaviour.Properties.copy(net.minecraft.world.level.block.Blocks.LECTERN)) }
}
