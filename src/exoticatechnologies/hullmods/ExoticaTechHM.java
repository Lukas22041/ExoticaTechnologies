package exoticatechnologies.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import exoticatechnologies.modifications.ShipModFactory;
import exoticatechnologies.modifications.ShipModLoader;
import exoticatechnologies.modifications.ShipModifications;
import exoticatechnologies.modifications.exotics.Exotic;
import exoticatechnologies.modifications.exotics.ExoticsHandler;
import exoticatechnologies.modifications.upgrades.Upgrade;
import exoticatechnologies.modifications.upgrades.UpgradesHandler;
import exoticatechnologies.util.ExtensionsKt;
import exoticatechnologies.util.FleetMemberUtils;
import exoticatechnologies.util.StringUtils;
import lombok.extern.log4j.Log4j;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Log4j
public class ExoticaTechHM extends BaseHullMod {
    private static final Color hullmodColor = new Color(94, 206, 226);

    public static void addToFleetMember(FleetMemberAPI member, ShipVariantAPI variant) {
        if (variant == null) {
            return;
        }

        ShipModifications mods = ShipModFactory.generateForFleetMember(member);

        if (variant.hasHullMod("exoticatech")) {
            variant.removePermaMod("exoticatech");
        }

        if (mods.shouldApplyHullmod()) {

            ExtensionsKt.fixVariant(member);
            variant.addPermaMod("exoticatech");

            for (String moduleVariantId : variant.getStationModules().keySet()) {
                ShipVariantAPI moduleVariant = variant.getModuleVariant(moduleVariantId);

                if (moduleVariant != null) {
                    moduleVariant.addPermaMod("exoticatech");
                }
            }

            member.updateStats();
        }
    }

    public static void addToFleetMember(FleetMemberAPI member) {
        addToFleetMember(member, member.getVariant());
    }

    public static void removeFromFleetMember(FleetMemberAPI member) {
        if (member.getVariant() == null) {
            return;
        }

        ShipVariantAPI shipVariant = member.getVariant();
        if (shipVariant.hasHullMod("exoticatech")) {
            shipVariant.removePermaMod("exoticatech");
        }
    }

    @Override
    public boolean affectsOPCosts() {
        return false;
    }

    @Override
    public Color getNameColor() {
        return hullmodColor;
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        ShipModifications mods = ShipModLoader.get(member, member.getVariant());
        if (mods == null) {
            member.getVariant().removePermaMod("exoticatech");
            return;
        }

        for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = mods.getUpgrade(upgrade);
            if (level <= 0) continue;
            upgrade.advanceInCampaign(member, mods, amount);
        }

        for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if (mods.hasExotic(exotic)) {
                exotic.advanceInCampaign(member, mods, amount, Objects.requireNonNull(mods.getExoticData(exotic)));
            }
        }
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        FleetMemberAPI member = FleetMemberUtils.findMemberFromShip(ship);
        if (member == null) return;

        ShipModifications mods = ShipModLoader.get(member, member.getVariant());
        if (mods == null) return;

        for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = mods.getUpgrade(upgrade);
            if (level <= 0) continue;

            upgrade.advanceInCombatUnpaused(ship, amount, member, mods);
        }

        for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if (!mods.hasExotic(exotic)) continue;

            exotic.advanceInCombatUnpaused(ship, amount, member, mods, Objects.requireNonNull(mods.getExoticData(exotic)));
        }
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        FleetMemberAPI member = FleetMemberUtils.findMemberForStats(stats);
        if (member == null) {
            return;
        }

        try {
            if (!stats.getVariant().getStationModules().isEmpty()) {
                FleetMemberUtils.moduleMap.clear();

                for (Map.Entry<String, String> e : stats.getVariant().getStationModules().entrySet()) {
                    ShipVariantAPI module = stats.getVariant().getModuleVariant(e.getKey());

                    FleetMemberUtils.moduleMap.put(module.getHullVariantId(), member);
                }
            }
        } catch (Exception e) {
            log.info("Failed to get modules", e);
        }

        ShipModifications mods = ShipModLoader.get(member, member.getVariant());

        if (mods == null) {
            member.getVariant().removePermaMod("exoticatech");
            return;
        }

        for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if (!mods.hasExotic(exotic)) continue;

            exotic.applyExoticToStats(id, stats, member, mods, Objects.requireNonNull(mods.getExoticData(exotic)));
        }

        for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            int level = mods.getUpgrade(upgrade);
            if (level <= 0) continue;

            upgrade.applyUpgradeToStats(stats, member, mods);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        FleetMemberAPI member = FleetMemberUtils.findMemberFromShip(ship);
        if (member == null) return;

        ShipModifications mods = ShipModLoader.get(member, member.getVariant());
        if (mods == null) return;

        for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if (!mods.hasExotic(exotic)) continue;
            exotic.applyToShip(id, member, ship, mods, Objects.requireNonNull(mods.getExoticData(exotic)));
        }

        for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if (!mods.hasUpgrade(upgrade)) continue;
            upgrade.applyToShip(member, ship, mods);
        }
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        FleetMemberAPI member = FleetMemberUtils.findMemberFromShip(ship);
        if (member == null) return;

        ShipModifications mods = ShipModLoader.get(member, member.getVariant());
        if (mods == null) return;

        for (Exotic exotic : ExoticsHandler.EXOTIC_LIST) {
            if (!mods.hasExotic(exotic)) continue;
            exotic.applyToFighters(member, ship, fighter, mods);
        }
        for (Upgrade upgrade : UpgradesHandler.UPGRADES_LIST) {
            if (!mods.hasUpgrade(upgrade)) continue;
            upgrade.applyToFighters(member, ship, fighter, mods);
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize, ShipAPI ship) {
        FleetMemberAPI fm = FleetMemberUtils.findMemberFromShip(ship);
        if (fm == null) return "SHIP NOT FOUND";
        if (fm.getShipName() == null) {
            return "SHIP MODULE";
        }
        return fm.getShipName();
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI hullmodTooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        FleetMemberAPI member = FleetMemberUtils.findMemberFromShip(ship);
        if (member == null) return;
        if (member.getShipName() == null) {
            hullmodTooltip.addPara("Ship modules do not support tooltips.", 0);
            return;
        }

        ShipModifications mods = ShipModLoader.get(member, member.getVariant());
        if (mods == null) return;

        boolean exoticsExpand = Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"));
        boolean upgradesExpand = Keyboard.isKeyDown(Keyboard.getKeyIndex("F2"));

        mods.populateTooltip(member, ship.getMutableStats(), hullmodTooltip, width, 500f, upgradesExpand, exoticsExpand, false);

        if (!exoticsExpand && !upgradesExpand) {
            StringUtils.getTranslation("CommonOptions", "ExpandExotics").addToTooltip(hullmodTooltip);
            StringUtils.getTranslation("CommonOptions", "ExpandUpgrades").addToTooltip(hullmodTooltip);
        } else {
            StringUtils.getTranslation("CommonOptions", "UnexpandInfo").addToTooltip(hullmodTooltip);
        }
    }

    public static void removeHullModFromVariant(ShipVariantAPI v) {
        v.removePermaMod("exoticatech");
        v.removeMod("exoticatech");
        v.removeSuppressedMod("exoticatech");

        List<String> slots = v.getModuleSlots();
        if (slots == null || slots.isEmpty()) return;

        for (String slot : slots) {
            ShipVariantAPI module = v.getModuleVariant(slot);
            if (module != null) {
                removeHullModFromVariant(module);
            }
        }
    }
}