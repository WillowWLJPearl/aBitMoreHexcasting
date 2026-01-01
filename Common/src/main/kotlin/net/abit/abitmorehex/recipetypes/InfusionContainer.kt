package net.abit.abitmorehex.recipetypes

import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack

class InfusionContainer : Container {
    override fun getContainerSize(): Int = 0
    override fun isEmpty(): Boolean = true
    override fun getItem(index: Int): ItemStack = ItemStack.EMPTY
    override fun removeItem(index: Int, count: Int): ItemStack = ItemStack.EMPTY
    override fun removeItemNoUpdate(index: Int): ItemStack = ItemStack.EMPTY
    override fun setItem(index: Int, stack: ItemStack) {}
    override fun setChanged() {}
    override fun stillValid(player: net.minecraft.world.entity.player.Player): Boolean = false
    override fun clearContent() {}
}