package net.abit.abitmorehex.registry


import dev.architectury.registry.client.rendering.RenderTypeRegistry
import net.abit.abitmorehex.blocks.EmptyJar
import net.abit.abitmorehex.blocks.RootedTableBlock
import net.abit.abitmorehex.blocks.SoulJar
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockStateProperties


object AbitmorehexBlocks : AbitmorehexRegistrar<Block>(
    Registries.BLOCK,
    {BuiltInRegistries.BLOCK}
) {
    val ROOTEDTABLEBLOCK: AbitmorehexRegistrar<Block>.Entry<RootedTableBlock> =
        register("rooted_table_block") { RootedTableBlock(BlockBehaviour.Properties.of()
            .noOcclusion()
            .randomTicks()
            .isViewBlocking { _, _, _ -> false }
            .isSuffocating { _, _, _ -> false }
            .isRedstoneConductor { _, _, _ -> false })
        }
    val EMPTYJAR: AbitmorehexRegistrar<Block>.Entry<EmptyJar> =
        register(
            "empty_jar",
        )
        {
            EmptyJar(BlockBehaviour.Properties.copy(Blocks.GLASS))
        }
    val SOULJAR: AbitmorehexRegistrar<Block>.Entry<SoulJar> =
        register(
            "soul_jar",
        )
        {
            SoulJar(BlockBehaviour.Properties.of()
                .noOcclusion()
                .randomTicks()
                .isViewBlocking { _, _, _ -> false }
                .isSuffocating { _, _, _ -> false }
                .isRedstoneConductor { _, _, _ -> false }
            )
        }

    override fun initClient() {
        RenderTypeRegistry.register(
            net.minecraft.client.renderer.RenderType.translucent(),
            AbitmorehexBlocks.EMPTYJAR.value,
            AbitmorehexBlocks.SOULJAR.value
        )
    }

}
