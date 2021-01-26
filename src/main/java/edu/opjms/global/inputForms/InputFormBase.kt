package edu.opjms.global.inputForms

import edu.opjms.templating.RawTypes
import edu.opjms.templating.inputPanes.InputPaneBase
import edu.opjms.templating.inputPanes.SelectFieldGroupPane
import edu.opjms.templating.inputPanes.SelectFieldPane
import edu.opjms.templating.inputPanes.TextFieldPane
import kotlinx.serialization.Serializable

@Serializable
sealed class RawInputFormBase {
    abstract fun toInputPane(): InputPaneBase
}

@Serializable
data class RawTextFieldInput(
        val labelText: String,
        val placeholderText: String,
        val rawType: RawTypes,
        val regex: String,
        val tooltipText: String,
        val isUniqueField: Boolean
): RawInputFormBase() {
    override fun toInputPane() =
            TextFieldPane(labelText, placeholderText, regex, tooltipText, rawType, isUniqueField)
}

@Serializable
data class RawSelectField(
        val labelText: String,
        val rawType: RawTypes,
        val pairs: Array<Pair<String, String>>?
): RawInputFormBase() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RawSelectField) return false


        if (labelText != other.labelText) return false
        if (rawType != other.rawType) return false
        if (pairs != null) {
            if (other.pairs == null) return false
            if (!pairs.contentEquals(other.pairs)) return false
        } else if (other.pairs != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = labelText.hashCode()
        result = 31 * result + rawType.hashCode()
        result = 31 * result + (pairs?.contentHashCode() ?: 0)
        return result
    }

    override fun toInputPane() = SelectFieldPane(labelText, rawType, pairs)
}

@Serializable
data class RawSelectGroup(
        val delimiter: String,
        val selects: List<RawSelectField>
): RawInputFormBase() {
    override fun toInputPane(): InputPaneBase {
        val list = List(selects.size) { selects[it].toInputPane() }
        return SelectFieldGroupPane(delimiter, list)
    }
}