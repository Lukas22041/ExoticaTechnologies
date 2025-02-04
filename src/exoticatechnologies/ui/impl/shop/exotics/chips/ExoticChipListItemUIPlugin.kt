package exoticatechnologies.ui.impl.shop.exotics.chips

import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.BaseTooltipCreator
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModifications
import exoticatechnologies.modifications.exotics.ExoticData
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipListItemUIPlugin
import exoticatechnologies.ui.lists.ListUIPanelPlugin
import exoticatechnologies.util.getMods
import lombok.Getter
import org.magiclib.kotlin.setAlpha

class ExoticChipListItemUIPlugin(
    item: CargoStackAPI,
    member: FleetMemberAPI,
    listPanel: ListUIPanelPlugin<CargoStackAPI>
) : ChipListItemUIPlugin(item, member, listPanel) {

    private var itemImage: TooltipMakerAPI? = null

    override fun showChip(rowPanel: CustomPanelAPI) {
        val plugin = getPlugin()
        val exotic = plugin.exoticData!!.exotic
        val type = plugin.exoticData!!.type

        itemImage = rowPanel.createUIElement(iconSize, iconSize, false)
        itemImage!!.addImage(plugin.spec.iconName, iconSize, 0f)
        rowPanel.addUIElement(itemImage).inLMid(0f)

        val itemInfo = rowPanel.createUIElement(panelWidth - iconSize, panelHeight, false)
        itemInfo.addPara(exotic.name, plugin.exoticData!!.getColor(), 0f).position.inTL(3f, 3f)
        itemInfo.addPara(type.name, type.colorOverlay.setAlpha(255), 0f)

        rowPanel.addUIElement(itemInfo).rightOfMid(itemImage, 3f)
    }

    private fun getPlugin(): ExoticSpecialItemPlugin {
        return item.plugin as ExoticSpecialItemPlugin
    }
}