package exoticatechnologies.ui.impl.shop.exotics.chips

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.modifications.upgrades.UpgradeSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListUIPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipSearcher
import java.awt.Color

class ExoticChipPanelUIPlugin(
    parentPanel: CustomPanelAPI,
    val exotic: Exotic,
    member: FleetMemberAPI,
    market: MarketAPI
) : ChipPanelUIPlugin<ExoticSpecialItemPlugin>(parentPanel, exotic, member, market) {
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun getChipSearcher(): ChipSearcher<ExoticSpecialItemPlugin> {
        return ExoticChipSearcher()
    }

    override fun getChipListPlugin(listPanel: CustomPanelAPI, member: FleetMemberAPI): ChipListUIPlugin {
        return ExoticChipListUIPlugin(parentPanel, member)
    }
}