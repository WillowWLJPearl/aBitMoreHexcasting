package net.abit.abitmorehex.casting.actions.spells

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingVM
import at.petrak.hexcasting.api.casting.iota.*
import at.petrak.hexcasting.api.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.api.item.IotaHolderItem
import at.petrak.hexcasting.api.mod.HexConfig
import net.abit.abitmorehex.registry.eval.SubCastingEnvironment
import net.abit.abitmorehex.registry.item.readIotaList
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.SlotAccess
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

object OpCastItemSpell : SpellAction {
    override val argc = 1

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        if(env is SubCastingEnvironment) {
            val slot = (args[0] as DoubleIota).double
            val container = env.getContainer()
            val castItem = container.getItem(slot.toInt())
            val vec3 = env.getContainerVec3()
            val cost: Long = HexConfig.common().dustMediaAmount()
            val particles = listOf(
                ParticleSpray.cloud(vec3, 1.0)
            )
            if(castItem.item !is IotaHolderItem)
                throw MishapBadItem(ItemEntity(
                    env.world,
                    vec3.x, vec3.y, vec3.z,
                    castItem.copy()
                ), Component.literal("a item with a iota"))
            return SpellAction.Result(Spell(castItem, slot.toInt(), vec3, env.world as ServerLevel), cost, particles)
        }
        val slot = (args[0] as DoubleIota).double
        val entity = env.castingEntity as Player
        val castItem = entity.inventory.getItem(slot.toInt())

        val cost: Long = HexConfig.common().chargedCrystalMediaAmount()

        val particles = listOf(
            ParticleSpray.cloud(entity.position(), 1.0)
        )
        if(castItem.item !is IotaHolderItem)
            throw MishapBadItem(ItemEntity(
                entity.level(),
                entity.x, entity.y, entity.z,
                castItem.copy()
            ), Component.literal("a item with a iota"))

        return SpellAction.Result(Spell(castItem, slot.toInt(), entity.position(), entity.level() as ServerLevel), cost, particles)
    }

    private data class Spell(val item: ItemStack, val slot: Int, val vec3: Vec3, val world: ServerLevel) : RenderedSpell {

        val holder = item.item as IotaHolderItem
        val Iotalist = readIotaList(item, world)
        val datum = holder.readIotaTag(item)
            ?.let { IotaType.deserialize(it, world) }
            ?: holder.emptyIota(item)
            ?:     throw MishapBadItem(ItemEntity(
                world,
                vec3.x, vec3.y, vec3.z,
                item.copy()
            ), Component.literal("a item with a subiota"))
        override fun cast(env: CastingEnvironment) {
            val level = env.world as ServerLevel
            val image: CastingImage = CastingImage()
            CastingVM(
                image = image,
                env = SubCastingEnvironment(level, vec3,slot, item )
            ).queueExecuteAndWrapIotas(
                iotas = Iotalist,
                world = level
            )
        }
    }
}
