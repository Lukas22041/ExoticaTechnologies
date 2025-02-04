package exoticatechnologies.modifications.conditions.impl

import com.fs.starfarer.api.combat.ShieldAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.conditions.OperatorCondition

/**
 * Big fat note: There's nothing i can do to detect modded ways to remove the shield.
 **/
class HasShield: OperatorCondition() {
    override val key = "hasShield"
    override fun getActual(member: FleetMemberAPI, mods: ShipModifications?): Any? {
        member.variant?.let {
            if (member.variant.hasHullMod("shield_shunt")) {
                return false;
            }

            if (member.variant.hasHullMod("frontshield")) {
                return true;
            }
        }

        return member.hullSpec.shieldType != ShieldAPI.ShieldType.PHASE
                && member.hullSpec.shieldType != ShieldAPI.ShieldType.NONE
    }
}