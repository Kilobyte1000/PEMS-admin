package edu.opjms.global.inputForms

import edu.opjms.templating.RawTypes
import edu.opjms.templating.inputPanes.*
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


sealed interface RawInputFormBase: java.io.Serializable {
    fun toInputPane(): InputPaneBase
    fun genHtml(): String
}

@JvmRecord
data class RawTextFieldInput(
        val labelText: String,
        val placeholderText: String,
        val rawType: RawTypes?,
        val regex: String,
        val tooltipText: String,
        val isUniqueField: Boolean,
        val isDuplicate: Boolean
): RawInputFormBase {


    override fun toInputPane() =
            TextFieldPane(labelText, placeholderText, regex, tooltipText, rawType, isUniqueField).apply { showDuplicateError(isDuplicate) }

    override fun genHtml(): String {
        val isRegexValid = kotlin.run {
            try {
                Pattern.compile(regex)
                true
            } catch (ignored: PatternSyntaxException) {
                false
            }
        }
        val message = genTextErrMessage(
                labelText.isBlank(),
                isDuplicate,
                placeholderText.isBlank(),
                rawType == null,
                isRegexValid,
                regex,
                labelText
        )
        return genTextInput(labelText, placeholderText, rawType, regex, tooltipText, message)
    }
}

@JvmRecord
data class RawSelectField(
        val labelText: String,
        val rawType: RawTypes,
        val fieldData: FieldData,
        val isDuplicate: Boolean
): RawInputFormBase {
    override fun toInputPane() = SelectFieldPane(labelText, rawType, fieldData, isDuplicate)
    override fun genHtml(): String {
        val message = genSelectErrMessage(
                isDuplicate,
                labelText,
                fieldData.duplicates(),
                if (rawType == RawTypes.NUMBER) fieldData.nonNumeric() else emptyList()
        )
        return genSelectInput(labelText, fieldData.pairs(), message)
    }
}

@JvmRecord
data class RawDateInput(val labelText: String, val isDuplicate: Boolean): RawInputFormBase {
    override fun toInputPane() =
            DateInputPane(labelText).apply { showDuplicateError(isDuplicate) }

    override fun genHtml(): String {
        val errMessage = genDateErrMessage(isDuplicate, labelText)
        return genDateInput(labelText, errMessage)
    }
}

@JvmRecord
data class RawBooleanField(val labelText: String, val trueLabel: String, val falseLabel: String, val isDuplicate: Boolean): RawInputFormBase {
    override fun genHtml(): String {
        return genBooleanInput(isDuplicate, labelText, trueLabel, falseLabel)
    }

    override fun toInputPane(): InputPaneBase {
        return BooleanFieldPane(labelText, trueLabel, falseLabel)
                .apply { showDuplicateError(isDuplicate) }
    }
}

