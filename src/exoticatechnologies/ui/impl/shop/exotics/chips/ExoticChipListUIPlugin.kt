package exoticatechnologies.ui.impl.shop.exotics.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.ui.impl.shop.chips.ChipListUIPlugin
import exoticatechnologies.ui.lists.ListItemUIPanelPlugin
import exoticatechnologies.util.StringUtils
import java.awt.Color

class ExoticChipListUIPlugin(
    parentPanel: CustomPanelAPI,
    member: FleetMemberAPI
) : ChipListUIPlugin(parentPanel, member) {
    override val listHeader = StringUtils.getTranslation("ExoticsDialog", "ChipsHeader").toString()
    override var bgColor: Color = Color(255, 70, 255, 0)

    override fun createPanelForItem(
        tooltip: TooltipMakerAPI,
        item: CargoStackAPI
    ): ListItemUIPanelPlugin<CargoStackAPI> {
        val rowPlugin = ExoticChipListItemUIPlugin(item, member, this)
        rowPlugin.panelWidth = panelWidth
        rowPlugin.panelHeight = rowHeight
        rowPlugin.layoutPanel(tooltip)
        return rowPlugin
    }
}