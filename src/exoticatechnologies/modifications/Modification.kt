package exoticatechnologies.modifications

import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import exoticatechnologies.modifications.conditions.Condition
import exoticatechnologies.modifications.conditions.ConditionDict
import exoticatechnologies.modifications.conditions.toList
import org.apache.log4j.Logger
import org.json.JSONObject
import org.lazywizard.lazylib.ext.json.optFloat
import java.awt.Color

abstract class Modification(val key: String, val settings: JSONObject) {
    companion object {
        @JvmStatic
        private val log = Logger.getLogger(Modification::class.java)
    }

    var name: String = settings.getString("name")
    var tags: List<String>

    open var color: Color = Color.white
    open var description: String = ""
    open var valueMult: Float = settings.optFloat("valueMult", 1.0f)
    protected abstract val icon: String
    val conditions: MutableList<Condition> = mutableListOf()

    init {
        if (settings.has("conditions")) {
            conditions.addAll(ConditionDict.getCondsFromJSONArray(settings.getJSONArray("conditions")))
        }

        if (settings.has("tag")) {
            tags = listOf(settings.getString("tag"))
        } else if (settings.has("tags")) {
            tags = settings.getJSONArray("tags").toList()
        } else {
            tags = listOf()
        }
    }

    /**
     * Checks tag against all modifications in mods.
     * @return true if null tag, null mods, or no conflicting mod was found. false if a conflicting mod was found.
     */
    open fun checkTags(member: FleetMemberAPI?, mods: ShipModifications?, tags: List<String>): Boolean {
        if (mods == null || tags.isEmpty()) {
            return true
        }

        return mods.getModsThatConflict(tags).none { it.key != this.key }
    }

    open fun checkConditions(member: FleetMemberAPI?, mods: ShipModifications?): Boolean {
        try {
            val requiredConditions = conditions
                .filter { !it.weightOnly }

            return requiredConditions
                .none { !it.compare(member!!, mods, key) }
        } catch (ex: Exception) {
            log.error("$name threw exception while checking conditions", ex)
            throw ex
        }

    }

    open fun getCannotApplyReasons(member: FleetMemberAPI, mods: ShipModifications?): List<String> {
        return conditions
            .filter { !it.weightOnly }
            .filter { !it.compare(member, mods, key) }
            .mapNotNull { it.cannotApplyReason }
    }

    open fun getCalculatedWeight(member: FleetMemberAPI, mods: ShipModifications?): Float {
        if (conditions.isNotEmpty()) {
            return 1f + conditions
                .map { it.calculateWeight(member, mods) }
                .sum()
        }
        return 1f
    }

    open fun shouldLoad(): Boolean {
        return true
    }

    open fun shouldShow(member: FleetMemberAPI, mods: ShipModifications, market: MarketAPI?): Boolean {
        return true
    }

    open fun canApply(member: FleetMemberAPI, mods: ShipModifications?): Boolean {
        val condCheck = checkConditions(member, mods)
        val tagsCheck = checkTags(member, mods, tags)
        val varCheck = canApplyToVariant(member.variant)
        return condCheck && tagsCheck && varCheck
    }

    open fun canApplyToVariant(variant: ShipVariantAPI): Boolean {
        return true
    }
}