package exoticatechnologies.ui.impl.shop.upgrades

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.upgrades.Upgrade
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class UpgradeListUIPlugin(
    parentPanel: CustomPanelAPI,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var market: MarketAPI?
): ListUIPanelPlugin<Upgrade>(parentPanel) {
    override val listHeader = StringUtils.getTranslation("UpgradesDialog", "OpenUpgradeOptions").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)
    private var modsValue: Float = ShipModLoader.get(member, variant)!!.getValue()

    override fun advancePanel(amount: Float) {
        val mods = ShipModLoader.get(member, variant)!!
        val newValue = mods.getValue()
        if (modsValue != newValue) {
            modsValue = newValue
            layoutPanels()
        }
    }

    override fun createPanelForItem(tooltip: TooltipMakerAPI, item: Upgrade): ListItemUIPanelPlugin<Upgrade> {
        val rowPlugin = UpgradeListItemUIPlugin(item, member, variant, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }

    override fun sortMembers(items: List<Upgrade>): List<Upgrade> {
        val mods = ShipModLoader.get(member, variant)!!
        return items.sortedWith { a, b ->
            if (mods.hasUpgrade(a))
                if (mods.hasUpgrade(b))
                    mods.getUpgrade(a) - mods.getUpgrade(b)
                else
                    -1
            else
                if (mods.hasUpgrade(b))
                    1
                else if (a.canApply(member, mods))
                    if (b.canApply(member, mods))
                        0
                    else
                        -1
                else if (b.canApply(member, mods))
                    1
            else
                0
        }
    }

    override fun shouldMakePanelForItem(item: Upgrade): Boolean {
        val mods = ShipModLoader.get(member, variant)!!

        if (mods.hasUpgrade(item)) {
            return true
        }

        if (market == null) {
            return false
        }
        
        return item.shouldShow(member, mods, market)
    }
}