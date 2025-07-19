package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getItemEntity
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.extractMedia
import at.petrak.hexcasting.api.utils.isMediaItem
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.abit.abitmorehex.blockentities.RootedTable
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.item.ItemEntity

/**
 * Recharge a RootedTable BE by extracting media from an ItemEntity.
 * Arg0: Vec3 for altar location
 * Arg1: EntityIota of the ItemEntity containing media
 */
object OpChargeAltar : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        val vec3 = (args[0] as Vec3Iota).vec3
        // Get the ItemEntity and check range
        val itemEntity = args.getItemEntity(1, argc)
        env.assertEntityInRange(itemEntity)

        // Find the RootedTable at the target position
        val pos = BlockPos.containing(vec3.x, vec3.y, vec3.z)
        val server = env.world as? ServerLevel
        val table = server
            ?.getBlockEntity(pos) as? RootedTable
            ?: throw MishapBadItem.of(itemEntity, "table")

        // Verify the item holds media
        val stack = itemEntity.item
        if (!isMediaItem(stack)) {
            throw MishapBadItem.of(itemEntity, "media")
        }

        // Cost: one shard unit
        val cost = MediaConstants.SHARD_UNIT
        val particles = listOf(ParticleSpray.burst(itemEntity.position(), 0.5))

        return SpellAction.Result(Spell(itemEntity, table), cost, particles)
    }

    private data class Spell(
        val entity: ItemEntity,
        val table: RootedTable
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            if (!entity.isAlive) return
            val world = env.world as? ServerLevel ?: return
            val stack = entity.item

            // Find the media holder on the item
            val holder = IXplatAbstractions.INSTANCE.findMediaHolder(stack) ?: return

            // Simulate how much we can extract (using infinite capacity)
            val simStack = stack.copy()
            val canTake  = extractMedia(simStack, Long.MAX_VALUE)
            if (canTake <= 0) return
            val taken    = extractMedia(stack, canTake)

            // Insert into the RootedTable
            val oldMedia = table.getMedia()
            table.setMedia(oldMedia + taken)
            table.setChanged()

            // Sync block update
            world.sendBlockUpdated(
                table.blockPos,
                table.blockState,
                table.blockState,
                3
            )

            // Update or remove the item entity
            entity.item = stack
            if (stack.isEmpty) entity.kill()
        }
    }
}
