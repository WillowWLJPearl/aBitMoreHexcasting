package net.abit.abitmorehex.client

import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.common.lib.HexItems
import com.mojang.datafixers.util.Pair
import net.abit.abitmorehex.blockentities.RootedTable
import net.abit.abitmorehex.blocks.RootedTableBlock
import net.abit.abitmorehex.registry.AbitmorehexBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object RegisterDisplays {
    fun register() {
        ScryingLensOverlayRegistry.addDisplayer(
            AbitmorehexBlocks.ROOTEDTABLEBLOCK.value,
            ScryingLensOverlayRegistry.OverlayBuilder { lines,
                                                        state: BlockState,
                                                        pos: BlockPos,
                                                        player: Player,
                                                        world: Level,
                                                        hitFace: Direction ->

                // grab your block entity
                val be = world.getBlockEntity(pos) as? RootedTable ?: return@OverlayBuilder

                // 1) Primary slot
                be.getPrimary().takeIf { it.isEmpty.not() }?.let { primary ->
                    lines.add(
                        Pair.of(
                            primary,
                            primary.item.getName(primary)
                        )
                    )
                }

                // 2) Iota slot
                be.getIotaSlot().takeIf { it.isEmpty.not() }?.let { iota ->
                    lines.add(
                        Pair.of(
                            iota,
                            IotaType.getDisplay(be.getIota())
                        )
                    )
                }

                // 3) Media (use a media icon, here an emerald as example)
                lines.add(
                    Pair.of(
                        ItemStack(HexItems.AMETHYST_DUST),
                        Component.literal("${be.getMedia()/ HexConfig.common().dustMediaAmount()} dust")
                    )
                )

                // 4) Mishap
                // if you stored your mishap as a Component:
                val mishapText: Component = be.getMishap()
                val text = mishapText.string
                text.let {
                    lines.add(
                        Pair.of(
                            ItemStack(Items.BARRIER),
                            Component.literal("Mishap: $it")
                        )
                    )
                }
            }
        )
    }
}