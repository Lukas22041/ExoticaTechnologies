package exoticatechnologies.ui.impl.shop.exotics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import exoticatechnologies.modifications.ShipModLoader
import exoticatechnologies.modifications.exotics.Exotic
import exoticatechnologies.modifications.exotics.ExoticSpecialItemPlugin
import exoticatechnologies.refit.RefitButtonAdder
import exoticatechnologies.ui.InteractiveUIPanelPlugin
import exoticatechnologies.ui.TimedUIPlugin
import exoticatechnologies.ui.impl.shop.chips.ChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.chips.ExoticChipPanelUIPlugin
import exoticatechnologies.ui.impl.shop.exotics.methods.*
import exoticatechnologies.util.RenderUtils
import java.awt.Color

class ExoticPanelUIPlugin(
    var parentPanel: CustomPanelAPI,
    var exotic: Exotic,
    var member: FleetMemberAPI,
    var variant: ShipVariantAPI,
    var market: MarketAPI?
) : InteractiveUIPanelPlugin() {
    private var mainPanel: CustomPanelAPI? = null
    private var descriptionPlugin: ExoticDescriptionUIPlugin? = null
    private var methodsPlugin: ExoticMethodsUIPlugin? = null
    private var resourcesPlugin: ExoticResourcesUIPlugin? = null
    private var chipsPlugin: ExoticChipPanelUIPlugin? = null
    private var chipsTooltip: TooltipMakerAPI? = null

    fun layoutPanels(): CustomPanelAPI {
        val panel = parentPanel.createCustomPanel(panelWidth, panelHeight, this)
        mainPanel = panel

        descriptionPlugin = ExoticDescriptionUIPlugin(panel, exotic, member, variant)
        descriptionPlugin!!.panelWidth = panelWidth / 2
        descriptionPlugin!!.panelHeight = panelHeight
        descriptionPlugin!!.layoutPanels().position.inTL(0f, 0f)

        val methods = getMethods()

        resourcesPlugin = ExoticResourcesUIPlugin(panel, exotic, member, variant, market, methods)
        resourcesPlugin!!.panelWidth = panelWidth / 2
        resourcesPlugin!!.panelHeight = panelHeight / 2
        resourcesPlugin!!.layoutPanels().position.inTR(0f, 0f)

        methodsPlugin = ExoticMethodsUIPlugin(panel, exotic, member, variant, market, methods)
        methodsPlugin!!.panelWidth = panelWidth / 2
        methodsPlugin!!.panelHeight = panelHeight / 2
        methodsPlugin!!.layoutPanels().position.inBR(0f, 0f)
        methodsPlugin!!.addListener(MethodListener())

        parentPanel.addComponent(panel).inTR(0f, 0f)

        return panel
    }

    private var setChipDescription = false
    override fun advancePanel(amount: Float) {
        chipsPlugin?.let {
            if (it.highlightedItem != null) {
                setChipDescription = true
                ExoticDescriptionUIPlugin.displayDescription = false
                descriptionPlugin!!.resetDescription(
                    ShipModLoader.get(member, variant)!!,
                    it.highlightedItem!!.exoticData!!
                )
            }
        }

        if (setChipDescription && chipsPlugin == null) {
            descriptionPlugin!!.resetDescription()
            setChipDescription = false
        }
    }

    private fun getMethods(): List<Method> {
        return mutableListOf(
            InstallMethod(),
            ChipMethod(),
            RecoverMethod(),
            DestroyMethod()
        )
    }

    fun checkedMethod(method: Method): Boolean {
        if (method is ChipMethod) {
            //do something else.
            showChipsPanel()
            return true
        } else {
            applyMethod(exotic, method)
            return false
        }
    }

    fun highlightedMethod(method: Method?): Boolean {
        resourcesPlugin!!.redisplayResourceCosts(method)
        return false
    }

    fun applyMethod(exotic: Exotic, method: Method) {
        val mods = ShipModLoader.get(member, variant)!!
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        method.apply(member, variant, mods, exotic, market)
        RefitButtonAdder.requiresVariantUpdate = true

        Global.getSoundPlayer().playUISound("ui_char_increase_skill_new", 1f, 1f)

        descriptionPlugin!!.resetDescription()
        resourcesPlugin!!.redisplayResourceCosts(method)
        methodsPlugin!!.showTooltip()
    }

    fun showChipsPanel() {
        methodsPlugin!!.destroyTooltip()
        resourcesPlugin!!.destroyTooltip()

        val pW = panelWidth / 2 - 6f
        val pH = panelHeight - 6f
        chipsTooltip = mainPanel!!.createUIElement(pW, pH, false)
        val innerPanel = mainPanel!!.createCustomPanel(pW, pH, null)

        chipsPlugin = ExoticChipPanelUIPlugin(innerPanel, exotic, member, market!!)
        chipsPlugin!!.panelWidth = pW
        chipsPlugin!!.panelHeight = pH
        chipsPlugin!!.layoutPanels().position.inTR(0f, 0f)

        chipsPlugin!!.addListener(ChipPanelListener())

        chipsTooltip!!.addCustom(innerPanel, 0f).position.inTL(0f, 0f)
        mainPanel!!.addUIElement(chipsTooltip).inTR(9f, 3f)
    }

    fun clickedChipPanelBackButton() {
        chipsPlugin!!.destroyTooltip()
        chipsPlugin = null
        mainPanel!!.removeComponent(chipsTooltip)
        chipsTooltip = null

        resourcesPlugin!!.redisplayResourceCosts(null)
        methodsPlugin!!.showTooltip()
    }

    fun clickedChipStack(stack: CargoStackAPI) {
        chipsPlugin!!.destroyTooltip()
        chipsPlugin = null
        mainPanel!!.removeComponent(chipsTooltip)
        chipsTooltip = null

        val method = ChipMethod()
        method.chipStack = stack

        applyMethod(exotic, method)
    }


    private inner class MethodListener : ExoticMethodsUIPlugin.Listener() {
        override fun checked(method: Method): Boolean {
            return this@ExoticPanelUIPlugin.checkedMethod(method)
        }

        override fun highlighted(method: Method): Boolean {
            return this@ExoticPanelUIPlugin.highlightedMethod(method)
        }

        override fun unhighlighted(method: Method): Boolean {
            return this@ExoticPanelUIPlugin.highlightedMethod(null)
        }
    }

    private inner class ChipPanelListener : ChipPanelUIPlugin.Listener<ExoticSpecialItemPlugin>() {
        override fun checkedBackButton() {
            this@ExoticPanelUIPlugin.clickedChipPanelBackButton()
        }

        override fun checked(stack: CargoStackAPI, plugin: ExoticSpecialItemPlugin) {
            this@ExoticPanelUIPlugin.clickedChipStack(stack)
        }
    }

    private class AppliedUIListener(val mainPlugin: ExoticPanelUIPlugin, val tooltip: TooltipMakerAPI) :
        TimedUIPlugin.Listener {
        override fun end() {
            mainPlugin.mainPanel!!.removeComponent(tooltip)
            mainPlugin.resourcesPlugin!!.redisplayResourceCosts(null)
            mainPlugin.methodsPlugin!!.showTooltip()
        }

        override fun render(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float) {

        }

        override fun renderBelow(pos: PositionAPI, alphaMult: Float, currLife: Float, endLife: Float) {
            RenderUtils.pushUIRenderingStack()
            val panelX = pos.x
            val panelY = pos.y
            val panelW = pos.width
            val panelH = pos.height
            RenderUtils.renderBox(
                panelX,
                panelY,
                panelW,
                panelH,
                Color.yellow,
                alphaMult * (endLife - currLife) / endLife
            )
            RenderUtils.popUIRenderingStack()
        }
    }
}