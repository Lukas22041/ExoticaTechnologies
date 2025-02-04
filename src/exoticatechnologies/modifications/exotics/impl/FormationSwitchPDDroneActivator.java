package exoticatechnologies.modifications.exotics.impl;

import activators.drones.DroneActivator;
import activators.drones.HoveringFormation;
import activators.drones.SpinningCircleFormation;
import com.fs.starfarer.api.combat.ShipAPI;

/**
 * Spawns four PD drones. The formation can be switched by key.
 */
public class FormationSwitchPDDroneActivator extends DroneActivator {
    public FormationSwitchPDDroneActivator(ShipAPI ship) {
        super(ship);
    }

    @Override
    public float getBaseActiveDuration() {
        return 0;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 2f;
    }

    @Override
    public float getBaseChargeRechargeDuration() {
        return 10f;
    }

    @Override
    public boolean usesChargesOnActivate() {
        return false;
    }

    @Override
    public boolean shouldActivateAI(float amount) {
        return false;
    }

    @Override
    public String getDisplayText() {
        return "PD Drones";
    }

    @Override
    public String getStateText() {
        if (getFormation() instanceof HoveringFormation) {
            return "HOVER";
        } else {
            return "CIRCLE";
        }
    }

    @Override
    public float getBarFill() {
        float fill = 0f;

        if (state == State.COOLDOWN) {
            fill = 1f - stateInterval.getElapsed() / stateInterval.getIntervalDuration();
        } else if (charges < getMaxCharges()) {
            fill = chargeInterval.getElapsed() / chargeInterval.getIntervalDuration();
        }

        return fill;
    }

    @Override
    public int getMaxCharges() {
        return 4;
    }

    @Override
    public int getMaxDeployedDrones() {
        return 4;
    }

    @Override
    public String getDroneVariant() {
        return "drone_pd_example";
    }

    @Override
    public void onActivate() {
        if (getFormation() instanceof HoveringFormation) {
            setFormation(new SpinningCircleFormation());
        } else {
            setFormation(new HoveringFormation());
        }
    }
}
