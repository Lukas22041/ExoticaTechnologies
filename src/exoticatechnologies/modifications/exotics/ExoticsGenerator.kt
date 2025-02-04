package exoticatechnologies.modifications.exotics

import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.skills.BaseSkillEffectDescription
import com.fs.starfarer.api.util.WeightedRandomPicker
import exoticatechnologies.config.FactionConfig
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.types.ExoticType
import exoticatechnologies.util.Utilities
import org.apache.log4j.Logger
import java.util.*

object ExoticsGenerator {
    private val log: Logger = Logger.getLogger(FactionConfig::class.java)

    @JvmStatic
    fun generate(member: FleetMemberAPI, mods: ShipModifications, context: ShipModFactory.GenerationContext): ETExotics {
        val config = context.factionConfig!!
        val allowedExotics: Map<Exotic, Float> = config.allowedExotics

        var exoticChance = config.exoticChance.toFloat() * getExoticChance(member)
        var exoticTypeChance = config.exoticTypeChance

        if (member.fleetData != null && member.fleetData.fleet != null) {
            if (member.fleetData.fleet.memoryWithoutUpdate.contains("\$exotica_exoticMult")) {
                exoticChance *= member.fleetData.fleet.memoryWithoutUpdate.getFloat("\$exotica_exoticMult")
            }

            if (member.fleetData.fleet.memoryWithoutUpdate.contains("\$exotica_exoticTypeMult")) {
                exoticTypeChance *= member.fleetData.fleet.memoryWithoutUpdate.getFloat("\$exotica_exoticTypeMult")
            }
        }

        val exotics = mods.exotics
        val smodCount = Utilities.getSModCount(member)
        exoticChance *= (1 + smodCount).toFloat()

        val random = ShipModFactory.random
        val rolledChance = random.nextFloat()
        if (rolledChance < exoticChance) {
            val perExoticMult = 1 + smodCount * 0.5f

            val exoticPicker = getExoticPicker(random, allowedExotics, member, mods)
            while (!exoticPicker.isEmpty && exotics.getCount(member) < config.getMaxExotics(member)) {
                val exotic = exoticPicker.pick(random)!!
                if (exotic.canApply(member, mods)) {
                    val roll = random.nextFloat()
                    val factionExoticWeight = allowedExotics[exotic]!!
                    val calculatedWeight = perExoticMult * factionExoticWeight * context.exoticChanceMult

                    if (roll < calculatedWeight) {
                        val data: ExoticData
                        if (random.nextFloat() <= exoticTypeChance) {
                            data = ExoticData(exotic, getTypePicker(random, context, exotic, config.allowedExoticTypes).pick() ?: ExoticType.NORMAL)
                        } else {
                            data = ExoticData(exotic)
                        }

                        data.mutateGenerationContext(context)

                        exotics.putExotic(data)
                    }
                }
                exoticPicker.remove(exotic)
            }
        }
        return exotics
    }

    private fun getExoticChance(member: FleetMemberAPI): Float {
        val sizeFactor: Float = when (member.hullSpec.hullSize) {
            ShipAPI.HullSize.CAPITAL_SHIP -> 1.5f
            ShipAPI.HullSize.CRUISER -> 1.25f
            ShipAPI.HullSize.DESTROYER -> 1.1f
            else -> 1.0f
        }

        return sizeFactor
    }

    fun getExoticPicker(random: Random, allowedExotics: Map<Exotic, Float>, member: FleetMemberAPI, mods: ShipModifications): WeightedRandomPicker<Exotic> {
        val exoticPicker = WeightedRandomPicker<Exotic>(random)
        allowedExotics.forEach { (exotic, factionChance) ->
            exoticPicker.add(exotic, factionChance * exotic.getGenerationChanceMult(member) * exotic.getCalculatedWeight(member, mods))
        }
        return exoticPicker
    }

    fun getExoticPicker(random: Random, allowedExotics: Map<Exotic, Float>): WeightedRandomPicker<Exotic> {
        val exoticPicker = WeightedRandomPicker<Exotic>(random)
        allowedExotics.forEach { (exotic, factionChance) ->
            exoticPicker.add(exotic, factionChance)
        }
        return exoticPicker
    }

    fun getTypePicker(
        random: Random,
        context: ShipModFactory.GenerationContext,
        exotic: Exotic,
        allowedExoticTypes: Map<ExoticType, Float>
    ): WeightedRandomPicker<ExoticType> {
        val typePicker = WeightedRandomPicker<ExoticType>(random)

        allowedExoticTypes.forEach { (exoticType, factionChance) ->
            if (exotic.canUseExoticType(exoticType)) {
                typePicker.add(exoticType, factionChance * exoticType.getChanceMult(context))
            }
        }

        if (typePicker.isEmpty) {
            typePicker.add(ExoticType.NORMAL)
        }
        
        return typePicker
    }

    fun getTypePicker(
        random: Random,
        exotic: Exotic,
        allowedExoticTypes: Map<ExoticType, Float>
    ): WeightedRandomPicker<ExoticType> {
        val typePicker = WeightedRandomPicker<ExoticType>(random)

        allowedExoticTypes.forEach { (exoticType, factionChance) ->
            if (exotic.canUseExoticType(exoticType)) {
                typePicker.add(exoticType, factionChance)
            }
        }

        if (typePicker.isEmpty) {
            typePicker.add(ExoticType.NORMAL)
        }

        return typePicker
    }
}