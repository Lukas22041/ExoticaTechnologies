package exoticatechnologies.util

import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.VariantSource
import exoticatechnologies.modifications.ShipModFactory
import exoticatechnologies.modifications.ShipModifications

fun FleetMemberAPI.getMods(): ShipModifications = ShipModFactory.generateForFleetMember(this)

fun ShipVariantAPI.getRefitVariant(): ShipVariantAPI {
    var shipVariant = this
    if (shipVariant.isStockVariant || shipVariant.source != VariantSource.REFIT) {
        shipVariant = shipVariant.clone()
        shipVariant.originalVariant = null
        shipVariant.source = VariantSource.REFIT
    }
    return shipVariant
}

fun FleetMemberAPI.fixVariant() {
    val newVariant = this.variant.getRefitVariant()
    if (newVariant != this.variant) {
        this.setVariant(newVariant, false, false)
    }

    newVariant.fixModuleVariants()
}

fun ShipVariantAPI.fixModuleVariants() {
    this.stationModules.forEach { (slotId, _) ->
        val moduleVariant = this.getModuleVariant(slotId)
        val newModuleVariant = moduleVariant.getRefitVariant()
        if (newModuleVariant != moduleVariant) {
            this.setModuleVariant(slotId, newModuleVariant)
        }

        newModuleVariant.fixModuleVariants()
    }
}