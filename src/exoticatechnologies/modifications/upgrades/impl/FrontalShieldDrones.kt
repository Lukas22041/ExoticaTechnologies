package exoticatechnologies.modifications.upgrades.impl

import activators.ActivatorManager
import activators.drones.DroneActivator
import activators.drones.DroneFormation
import activators.drones.PIDController
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.util.StringUtils
import org.json.JSONObject
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils

class FrontalShieldDrones(key: String, settings: JSONObject) : Upgrade(key, settings) {
    override var maxLevel: Int = 1

    override fun applyToShip(member: FleetMemberAPI, ship: ShipAPI, mods: ShipModifications) {
        ActivatorManager.addActivator(ship, FrontalShieldDroneActivator(ship))
    }

    override fun modifyToolTip(
        tooltip: TooltipMakerAPI,
        stats: MutableShipStatsAPI,
        member: FleetMemberAPI,
        mods: ShipModifications,
        expand: Boolean
    ) {
        val imageText = tooltip.beginImageWithText(icon, 64f)
        imageText.addPara("$name (%s)", 0f, color, mods.getUpgrade(this).toString())
        if (expand) {
            StringUtils.getTranslation("WaspDefenseDrones", "tooltip")
                .format("drones", 2)
                .addToTooltip(imageText)
        }
        tooltip.addImageWithText(5f)
    }

    override fun modifyInShop(tooltip: TooltipMakerAPI, member: FleetMemberAPI, mods: ShipModifications) {
        StringUtils.getTranslation("WaspDefenseDrones", "tooltip")
            .format("drones", 2)
            .addToTooltip(tooltip)
    }

    class FrontalShieldDroneActivator(ship: ShipAPI) : DroneActivator(ship) {

        override fun canAssignKey(): Boolean {
            return false
        }

        override fun getBaseActiveDuration(): Float {
            return 0f
        }

        override fun getBaseCooldownDuration(): Float {
            return 0f
        }

        override fun shouldActivateAI(amount: Float): Boolean {
            return canActivate()
        }

        override fun getBaseChargeRechargeDuration(): Float {
            return 20f
        }

        override fun canActivate(): Boolean {
            return false
        }

        override fun getDisplayText(): String {
            return "Barrier Drones"
        }

        override fun getStateText(): String {
            return ""
        }

        override fun getBarFill(): Float {
            var fill = 0f
            if (charges < maxCharges) {
                fill = chargeInterval.elapsed / chargeInterval.intervalDuration
            }
            return fill
        }

        override fun getMaxCharges(): Int {
            return 2
        }

        override fun getMaxDeployedDrones(): Int {
            return 2
        }

        override fun usesChargesOnActivate(): Boolean {
            return false
        }

        override fun getDroneFormation(): DroneFormation {
            return FrontalFormation()
        }

        override fun getDroneVariant(): String {
            return "wasp_single_wing"
        }
    }

    class FrontalFormation : DroneFormation() {
        fun isLeftDrone(drone: ShipAPI, index: Int): Boolean {
            return index == 1
        }

        override fun advance(ship: ShipAPI, drones: Map<ShipAPI, PIDController>, amount: Float) {
            drones.onEachIndexed { index, (drone, controller) ->
                var shipLoc = ship.location
                var angle = ship.facing
                if (isLeftDrone(drone, index)) {
                    angle += 30
                } else {
                    angle -= 30
                }
                var point = MathUtils.getPointOnCircumference(shipLoc, ship.collisionRadius * 1.5f, angle)
                controller.move(point, drone)

                var iter = Global.getCombatEngine().shipGrid.getCheckIterator(drone.location, 1000f, 1000f)

                var target: ShipAPI? = null
                var distance = 100000f
                for (it in iter) {
                    if (it is ShipAPI) {
                        if (it.isFighter) continue
                        if (Global.getCombatEngine().getFleetManager(it.owner).owner == Global.getCombatEngine()
                                .getFleetManager(drone.owner).owner
                        ) continue
                        if (it.isHulk) continue
                        var distanceBetween = MathUtils.getDistance(it, ship)
                        if (distance > distanceBetween) {
                            distance = distanceBetween
                            target = it
                        }
                    }
                }

                if (target != null) {
                    controller.rotate(Misc.getAngleInDegrees(drone.location, target.location), drone)
                } else {
                    controller.rotate(ship.facing + MathUtils.getRandomNumberInRange(-10f, 10f), drone)
                }
            }
        }
    }
}