package net.abit.abitmorehex.casting.actions.rw.armor

import at.petrak.hexcasting.api.addldata.ADIotaHolder
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.network.chat.Component

object OpWriteHeels : SpellAction {
    override val argc = 2

    override fun execute(args: List<Iota>, env: CastingEnvironment): SpellAction.Result {
        // 1) Arg 0 must be an EntityIota holding a LivingEntity
        val rawEnt = args.getOrNull(0)
            ?: throw MishapInvalidIota(args[0], 1, Component.literal("EntityIota"))
        val entIota = rawEnt as? EntityIota
            ?: throw MishapInvalidIota(rawEnt, 1, Component.literal("EntityIota"))
        val living = entIota.entity as? LivingEntity
            ?: throw MishapInvalidIota(rawEnt, 1, Component.literal("LivingEntity"))

        // 2) Arg 1 is the Iota to write
        val datum = args.getOrNull(1)
            ?: throw MishapInvalidIota(args[1], 0, Component.literal("Iota"))

        // 3) Grab the helmet stack
        val helmetStack = living.getItemBySlot(EquipmentSlot.FEET)

        // 4) Find its Iota holder
        val datumHolder: ADIotaHolder = IXplatAbstractions.INSTANCE
            .findDataHolder(helmetStack)
            ?: throw MishapBadOffhandItem.of(helmetStack, "iota.write")

        // 5) Test-write (simulate) to catch readonly etc
        if (!datumHolder.writeIota(datum, /*simulate=*/true)) {
            throw MishapBadOffhandItem.of(helmetStack, "iota.readonly", datum.display())
        }

        // 6) Check for “Other’s Name” mishap
        val trueName = MishapOthersName.getTrueNameFromDatum(
            datum, env.castingEntity as? ServerPlayer
        )
        if (trueName != null) {
            throw MishapOthersName(trueName)
        }

        // 7) All good — schedule a zero‐cost spell that actually writes
        return SpellAction.Result(
            Spell(datum, datumHolder),
            /* cost = */ 0L,
            /* particles = */ emptyList()
        )
    }

    private data class Spell(
        val datum: Iota,
        val holder: ADIotaHolder
    ) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            // Actually write into the helmet (simulate=false)
            holder.writeIota(datum, /*simulate=*/false)
        }
    }
}
