package exoticatechnologies.modifications.upgrades

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CargoTransferHandlerAPI
import com.fs.starfarer.api.campaign.SpecialItemPlugin
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exoticatechnologies.modifications.ModSpecialItemPlugin
import exoticatechnologies.util.RenderUtils
import exoticatechnologies.util.RomanNumeral
import org.lazywizard.lazylib.ui.LazyFont
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class UpgradeSpecialItemPlugin : ModSpecialItemPlugin() {
    var upgradeLevel = 0
    var upgrade: Upgrade? = null
        get() {
            if (field == null) {
                field = UpgradesHandler.UPGRADES[modId]!!
            }
            return field
        }
    override fun getName(): String {
        return String.format("%s - %s (%s)", super.getName(), upgrade!!.name, upgradeLevel)
    }

    override val type: ModType
        get() = ModType.UPGRADE
    override val sprite: SpriteAPI
        get() = Global.getSettings().getSprite("upgrades", upgrade!!.key)

    override fun createTooltip(
        tooltip: TooltipMakerAPI,
        expanded: Boolean,
        transferHandler: CargoTransferHandlerAPI,
        stackSource: Any,
        useGray: Boolean
    ) {
        val opad = 10.0f
        tooltip.addTitle(this.name)

        val design = this.designType
        Misc.addDesignTypePara(tooltip, design, opad)
        if (!spec.desc.isEmpty()) {
            var c = Misc.getTextColor()
            if (useGray) {
                c = Misc.getGrayColor()
            }
            tooltip.addPara(spec.desc, c, opad)
        }

        tooltip.addPara(upgrade!!.description, Misc.getTextColor(), opad)
    }

    override fun render(
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        alphaMult: Float,
        glowMult: Float,
        renderer: SpecialItemPlugin.SpecialItemRendererAPI
    ) {
        super.render(x, y, w, h, alphaMult, glowMult, renderer)

        val tX = 0.57f
        val tY = 0.7f
        RenderUtils.addText(RomanNumeral.toRoman(upgradeLevel), Color(255, 255, 255), Vector2f(x + (1 * tX) * w, y + (1 * tY) * h), LazyFont.TextAlignment.RIGHT)
    }

    override fun handleParam(index: Int, param: String, stack: CargoStackAPI) {
        when (Param[index]) {
            Param.UPGRADE_ID -> {
                modId = param
                if (UpgradesHandler.UPGRADES.containsKey(modId)) {
                    upgrade = UpgradesHandler.UPGRADES[modId]
                }
            }

            Param.UPGRADE_LEVEL -> upgradeLevel = param.toInt()
            Param.IGNORE_CRATE -> ignoreCrate = java.lang.Boolean.parseBoolean(param)
        }
    }

    private enum class Param {
        UPGRADE_ID, UPGRADE_LEVEL, IGNORE_CRATE;

        companion object {
            operator fun get(index: Int): Param {
                return values()[index]
            }
        }
    }
}