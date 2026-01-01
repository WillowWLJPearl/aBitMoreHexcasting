package net.abit.abitmorehex.registry.item

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.utils.*
import at.petrak.hexcasting.client.ClientTickCounter
import at.petrak.hexcasting.common.items.storage.ItemFocus
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.abit.abitmorehex.misc.ItemTrackerData
import net.abit.abitmorehex.misc.SubIotaHolderItem
import net.abit.abitmorehex.casting.iota.ItemIota
import net.abit.abitmorehex.networking.AbitmorehexNetworking
import net.abit.abitmorehex.networking.msg.CastFailureMessage
import net.abit.abitmorehex.networking.msg.CastSuccessMessage
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.*
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.*
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import java.util.*

class ItemShiftingMedia(properties: Properties) : Item(properties), IotaHolderItem, SubIotaHolderItem {
    companion object {
        private const val MIMIC_KEY = "mimics"
        private const val RESISTANCE_KEY = "resistance"

        /** Hashes every slot *except* `skipSlot` */
        private fun hashInventory(inv: Container, skipSlot: Int): Int {
            var h = inv.containerSize * 31
            for (i in 0 until inv.containerSize) {
                if (i == skipSlot) continue  // ignore the driving stack!
                val st = inv.getItem(i)
                if (!st.isEmpty) {
                    h = h * 31 + st.item.hashCode()
                    h = h * 31 + st.count
                    h = h * 31 + (st.tag?.hashCode() ?: 0)
                }
            }
            return h
        }



        /** Returns the slot index of `stack` in `inv`, or –1 if not found */
         fun findSlotByReference(inv: Container, target: ItemStack): Int {
            for (i in 0 until inv.containerSize) {
                if (inv.getItem(i) === target && inv.getItem(i).tag === target.tag) {
                    return i // exact same instance
                }
            }
            return -1
        }

         fun onInventoryChanged(stack : ItemStack, vec3: Vec3, inventory : Container, world: Level) {
            for(item in getTargets(stack)) {
                if(item == Items.REPEATER) {
                    val level = world as ServerLevel
                    val image: CastingImage = CastingImage()
                    val Iotalist = readIotaList(stack, world)
                    val slot = findSlotByReference(inventory, stack)
                    CastingVM(
                        image = image,
                        env = SubCastingEnvironment(level, vec3, slot, stack)
                    ).queueExecuteAndWrapIotas(
                        iotas = Iotalist,
                        world = level
                    )
                }
            }
        }

        public fun containerTick(
            stack: ItemStack,
            vec3: Vec3,
            inventory: Container,
            world: Level
        ) {
            val tag = stack.getOrCreateTag()

            // 1) find the one slot that holds *this* exact stack‑content
            val slotIndex = findSlotByReference(inventory, stack)
            if (slotIndex < 0) return  // not found, bail out

            // 2) compute a hash skipping that slot
            val currentHash = hashInventory(inventory, skipSlot = slotIndex)

            // 3) compare, update, and fire change once
            val lastHash = tag.getInt("lastHash")
            if (currentHash != lastHash) {
                tag.putInt("lastHash", currentHash)
                onInventoryChanged(stack, vec3, inventory, world)
            }
        }

        private fun getTargets(stack: ItemStack): List<Item> {
            val tag = stack.tag ?: return emptyList()
            // Retrieve ListTag of strings (type code 8)
            val list = tag.getList("mimics", Tag.TAG_STRING)  // returns ListTag :contentReference[oaicite:7]{index=7}
            return list
                .mapNotNull { it.asString }                   // get each string entry :contentReference[oaicite:8]{index=8}
                .mapNotNull { idStr ->
                    ResourceLocation.tryParse(idStr)          // parse into ResourceLocation
                }
                .mapNotNull { rl ->
                    BuiltInRegistries.ITEM.getOptional(rl).orElse(null)// lookup item in registry
                }
        }
    }

    override fun getName(stack: ItemStack): Component {
        // 1. Get the “real” name (from the first mimic target or fallback)
        val baseName = getTargets(stack)
            .firstOrNull()
            ?.getName(stack)
            ?: super.getName(stack)

        // 2. Create an obfuscated suffix
        val obfSuffix = Component.literal("??")
            .withStyle { it.withObfuscated(true) }

        // 3. Combine them into one component
        return Component.literal("")
            .append(obfSuffix.copy())
            .append(baseName)
            .append(obfSuffix)
    }

    /** Mojmap: use(Level, Player, InteractionHand): InteractionResultHolder<ItemStack> */
    override fun use(
        world: Level,
        user: Player,
        hand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        val stack = user.getItemInHand(hand)
        for (target in getTargets(stack)) {
            val result = target.use(world, user, hand)
            // on Mojmap, check != PASS to see if it handled the action
            if (result.result != InteractionResult.PASS) return result
        }
        return super.use(world, user, hand)
    }

    /** Mojmap: useOn(UseOnContext): boolean */
    override fun useOn(context: UseOnContext): InteractionResult {
        for (target in getTargets(context.itemInHand)) {
            val result = target.useOn(context)
            if (result != InteractionResult.PASS) return result
        }
        return super.useOn(context)
    }


    /** Mojmap: mineBlock(ItemStack, Level, BlockState, BlockPos, LivingEntity): boolean */
    override fun mineBlock(
        stack: ItemStack,
        world: Level,
        state: BlockState,
        pos: BlockPos,
        miner: LivingEntity
    ): Boolean {
        for (target in getTargets(stack)) {
            if (target.mineBlock(stack, world, state, pos, miner)) {
                return true
            }
        }
        return super.mineBlock(stack, world, state, pos, miner)
    }


    /** Mojmap: inventoryTick(ItemStack, Level, Entity, int, boolean): void */
    override fun inventoryTick(
        stack: ItemStack,
        world: Level,
        entity: Entity,
        slot: Int,
        selected: Boolean
    ) {
        if (world.isClientSide) return
        val targets = getTargets(stack)
        if (targets.isNotEmpty()) {
            // Full mimic behavior: let each target do its tick
            for (target in targets) {
                target.inventoryTick(stack, world, entity, slot, selected)
            }
        } else {
            if (world.isClientSide || entity !is ServerPlayer) {
                super.inventoryTick(stack, world, entity, slot, selected)
                return
            }
            val player = entity as ServerPlayer
            val serverWorld = world as ServerLevel

            // 1) Init resistance once
            val tag = stack.getOrCreateTag()
            if (!tag.contains(RESISTANCE_KEY)) {
                tag.putInt(RESISTANCE_KEY, 3)
                super.inventoryTick(stack, world, entity, slot, selected)
                return
            }
            val remaining = tag.getInt(RESISTANCE_KEY)

            // 2) Scan for any holder to write into
            for (itemStack in player.inventory.items) {
                if (itemStack.item !is IotaHolderItem) continue

                val holder = itemStack.item as IotaHolderItem
                if (!holder.writeable(itemStack)) continue

                // 2a) If resistance ≤ 0 → write into any
                if (remaining <= 0) {
                    val uuid = UUID.randomUUID()
                    val data = ItemTrackerData.get(serverWorld)
                    data.map[uuid] = stack.copy()
                    data.setDirty()

                    IXplatAbstractions.INSTANCE
                        .findDataHolder(itemStack)
                        ?.writeIota(ItemIota(uuid), false)
                        ?: throw MishapBadOffhandItem.of(itemStack, "iota.write")

                    player.inventory.removeItem(stack)
                    AbitmorehexNetworking.CHANNEL.sendToPlayer(
                        player,
                        CastSuccessMessage
                    )
                    super.inventoryTick(stack, world, entity, slot, selected)
                    return
                }

                // 2b) Else resistance > 0 → only write into *empty* holders
                val dataTag = itemStack.tag?.getCompound(ItemFocus.TAG_DATA)
                val isEmpty = dataTag == null || dataTag.isEmpty
                if (isEmpty) {
                    val uuid = UUID.randomUUID()
                    val data = ItemTrackerData.get(serverWorld)
                    data.map[uuid] = stack.copy()
                    data.setDirty()

                    IXplatAbstractions.INSTANCE
                        .findDataHolder(itemStack)
                        ?.writeIota(ItemIota(uuid), false)
                        ?: throw MishapBadOffhandItem.of(itemStack, "iota.write")

                    player.inventory.removeItem(stack)
                    AbitmorehexNetworking.CHANNEL.sendToPlayer(
                        player,
                        CastSuccessMessage
                    )
                    super.inventoryTick(stack, world, entity, slot, selected)
                    return
                }
            }

            // 3) Nothing accepted it → decrement resistance & spawn drop
            tag.putInt(RESISTANCE_KEY, remaining - 1)
            val itementity = ItemEntity(
                serverWorld,
                player.x, player.y, player.z,
                stack.copy()
            ).apply {
                setPickUpDelay(20)
                isNoGravity = true
            }

            // Only remove & drop if still in inventory
            if (player.inventory.contains(stack)) {
                player.inventory.removeItem(stack)
                serverWorld.addFreshEntity(itementity)

                AbitmorehexNetworking.CHANNEL.sendToPlayer(
                    player,
                    CastFailureMessage
                )
            }
        }
        val player = entity as ServerPlayer
        containerTick(stack, player.position(), player.inventory, player.level())
        super.inventoryTick(stack, world, entity, slot, selected)
    }

    /** Returns the slot index of `stack` in `inv`, or –1 if not found */
    private fun findSlotByReference(inv: Container, target: ItemStack): Int {
        for (i in 0 until inv.containerSize) {
            if (inv.getItem(i) === target && inv.getItem(i).tag === target.tag) {
                return i // exact same instance
            }
        }
        return -1
    }



    /**
     * Snapshots *every* slot of `inv` into a CompoundTag under:
     *   "Size"  → Int containerSize
     *   "Items" → ListTag of CompoundTags, each with:
     *       "Slot" (Byte) and optionally "Item" (CompoundTag)
     */
    fun saveContainer(inv: Container, debug: Boolean = false): CompoundTag {
        val root = CompoundTag()
        root.putInt("Size", inv.containerSize)

        val list = ListTag()
        for (i in 0 until inv.containerSize) {
            val slotTag = CompoundTag().apply {
                putByte("Slot", i.toByte())
                val stack = inv.getItem(i)
                if (!stack.isEmpty) {
                    put("Item", stack.save(CompoundTag()))
                }
            }
            list.add(slotTag)
        }
        root.put("Items", list)

        if (debug) {
            println("=== saveContainer (${inv.javaClass.simpleName}) ===")
            println(root)
        }

        return root
    }

    /**
     * Loads the NBT you wrote with saveContainer into an *existing* Container.
     * Clears the container first, then sets each slot.
     */
    fun loadContainer(tag: CompoundTag): SimpleContainer {
        // 1) Grab the list of saved slots (each a CompoundTag)
        val items = tag.getList("Items", Tag.TAG_COMPOUND.toInt())

        // 2) Figure out how big the inventory must be:
        //    find the max Slot index in the list, then +1
        var maxSlot = -1
        for (i in 0 until items.size) {
            val slotIdx = items.getCompound(i).getByte("Slot").toInt() and 0xFF
            if (slotIdx > maxSlot) maxSlot = slotIdx
        }
        val size = maxSlot + 1

        // 3) Create the container of exactly that size
        val inv = SimpleContainer(size)

        // 4) Fill each slot
        for (i in 0 until items.size) {
            val slotTag = items.getCompound(i)
            val slotIdx = slotTag.getByte("Slot").toInt() and 0xFF
            // skip any weird out‑of‑bounds just in case
            if (slotIdx !in 0 until size) continue

            val stack = if (slotTag.contains("Item", Tag.TAG_COMPOUND.toInt())) {
                ItemStack.of(slotTag.getCompound("Item"))
            } else {
                ItemStack.EMPTY
            }
            inv.setItem(slotIdx, stack)
        }

        return inv
    }


    /**
     * Returns a set containing every superclass and interface
     * implemented by the target Item, in order from most‑specific
     * to most‑general.
     */
    private fun getAllMimickedClasses(stack: ItemStack): Set<Class<*>> {
        val result = mutableSetOf<Class<*>>()
        for (target in getTargets(stack)) {
            var current: Class<*>? = target.javaClass
            while (current != null && current != Any::class.java) {
                result += current
                result += current.interfaces
                current = current.superclass
            }
        }
        return result
    }

    override fun readIotaTag(stack: ItemStack): CompoundTag? {
        return stack.getCompound(ItemFocus.TAG_DATA)
    }

    override fun emptyIota(stack: ItemStack?): Iota? {
       return null
    }

    override fun getColor(stack: ItemStack?): Int {
        val tag = stack?.tag
        if (tag != null && tag.contains(SubIotaHolderItem.TAG_OVERRIDE_VISUALLY, /*type=*/8)) {
            val override = tag.getString(SubIotaHolderItem.TAG_OVERRIDE_VISUALLY)
            if (override.isNotBlank() &&
                ResourceLocation.isValidResourceLocation(override)) {
                HexIotaTypes.REGISTRY[ResourceLocation(override)]?.let {
                    return it.color()
                }
            }
            // rainbow cycle fallback
            val hue = (ClientTickCounter.getTotal() % 360) / 360f
            return 0xFF000000.toInt() or (Mth.hsvToRgb(hue, 0.75f, 1f) and 0x00FFFFFF)
        }

        val dataTag = stack?.let { readSubIotaTag(it) } ?: return SubIotaHolderItem.DEFAULT_ERROR_COLOR
        return IotaType.getColor(dataTag)
    }


    override fun writeable(stack: ItemStack?): Boolean {
        if(shouldAllowIotaMimic(stack as ItemStack)) return false
        return true
    }

    override fun canWrite(
        stack: ItemStack?,
        iota: Iota?
    ): Boolean {
        if(shouldAllowIotaMimic(stack as ItemStack)) return false
        return true
    }

    override fun writeDatum(stack: ItemStack, datum: Iota?) {

        if(shouldAllowIotaMimic(stack)) return;
        if (datum == null) {
            stack.removeTagKey(ItemFocus.TAG_DATA)
            stack.removeTagKey(ItemFocus.TAG_SEALED)
        } else if (!ItemFocus.isSealed(stack)) {
            stack.putTag(ItemFocus.TAG_DATA, IotaType.serialize(datum))
        }
    }
    override fun appendHoverText(
        pStack: ItemStack,
        pLevel: Level?,
        pTooltipComponents: MutableList<Component>,
        pIsAdvanced: TooltipFlag
    ) {
        val items: List<Item> = getTargets(pStack)
        pTooltipComponents.add(Component.literal("Memory:").withStyle(ChatFormatting.GOLD,ChatFormatting.BOLD))
        for (item in items) {
            pTooltipComponents.add(Component.translatable(item.descriptionId).withStyle(ChatFormatting.AQUA))
        }
        if(!shouldAllowIotaMimic(pStack)) {
            pTooltipComponents.add(Component.literal("Primary").withStyle(ChatFormatting.LIGHT_PURPLE).underline)
            IotaHolderItem.appendHoverText(this, pStack, pTooltipComponents, pIsAdvanced)
        }
        if(!shouldAllowSubIotaMimic(pStack)) {
            pTooltipComponents.add(Component.literal("Secondary").withStyle(ChatFormatting.DARK_PURPLE).underline)
            SubIotaHolderItem.appendSecondaryHoverText(this, pStack, pTooltipComponents, pIsAdvanced)
        }
    }
    /**
     * Returns false if at least one of the mimicked classes implements IotaHolderItem,
     * otherwise true.
     */
    private fun shouldAllowIotaMimic(stack: ItemStack): Boolean {
        // 1. Get all classes/interfaces of the target
        val allTypes: Set<Class<*>> = getAllMimickedClasses(stack)

        // 2. Check if any type is assignable from IotaHolderItem
        val hasIotaHolder = allTypes.any { IotaHolderItem::class.java.isAssignableFrom(it) }

        // 3. If at least one implements it, we return false; otherwise true
        return !hasIotaHolder
    }
    private fun shouldAllowSubIotaMimic(stack: ItemStack): Boolean {
        // 1. Get all classes/interfaces of the target
        val allTypes: Set<Class<*>> = getAllMimickedClasses(stack)

        // 2. Check if any type is assignable from IotaHolderItem
        val hasSubIotaHolderItem = allTypes.any { SubIotaHolderItem::class.java.isAssignableFrom(it) }

        // 3. If at least one implements it, we return false; otherwise true
        return !hasSubIotaHolderItem
    }

    override fun readSubIotaTag(stack: ItemStack): CompoundTag? {
        return stack.getCompound(SubIotaHolderItem.TAG_DATA)
    }

    override fun writeablesub(stack: ItemStack): Boolean {
        if(shouldAllowSubIotaMimic(stack as ItemStack)) return false
        return true
    }

    override fun canWritesub(stack: ItemStack, iota: Iota?): Boolean {
        if(shouldAllowSubIotaMimic(stack as ItemStack)) return false
        return true
    }

    override fun writesubDatum(stack: ItemStack, iota: Iota?) {
        if (iota == null) {
            stack.removeTagKey(SubIotaHolderItem.TAG_DATA)
        } else if (!ItemFocus.isSealed(stack)) {
            stack.putTag(SubIotaHolderItem.TAG_DATA, IotaType.serialize(iota))
        }
    }
}