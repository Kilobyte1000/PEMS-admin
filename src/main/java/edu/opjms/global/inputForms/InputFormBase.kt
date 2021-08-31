package edu.opjms.global.inputForms

import edu.opjms.templating.RawTypes
import edu.opjms.templating.inputPanes.*
import kotlinx.serialization.Serializable
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException


interface RawInputFormBase{
    fun toInputPane(): InputPaneBase
    fun genHtml(): String
}

@Serializable
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

/*    override fun writeExternal(out: ObjectOutput) {
        with(out) {
            writeUTF(labelText)
            writeUTF(placeholderText)
            writeObject(rawType)
            writeUTF(regex)
            writeUTF(tooltipText)
            writeBoolean(isUniqueField)
            writeBoolean(isDuplicate)
        }
    }

    override fun readExternal(input: ObjectInput) {
        with(input) {
            labelText = readUTF()
        }
    }*/
}

@Serializable
data class RawSelectField(
        val labelText: String,
        val rawType: RawTypes,
        val fieldData: MultiOptionEditor.FieldData,
        val isDuplicate: Boolean
): RawInputFormBase {
    override fun toInputPane() = SelectFieldPane(labelText, rawType, fieldData, isDuplicate)
    override fun genHtml(): String {
        val message = genSelectErrMessage(
                isDuplicate,
                labelText,
                fieldData.duplicates,
                if (rawType == RawTypes.NUMBER) fieldData.nonNumeric else emptyList()
        )
        return genSelectInput(labelText, fieldData.pairs, message)
    }
}

@Serializable
data class RawDateInput(val labelText: String, val isDuplicate: Boolean): RawInputFormBase {
    override fun toInputPane() =
            DateInputPane(labelText).apply { showDuplicateError(isDuplicate) }

    override fun genHtml(): String {
        val errMessage = genDateErrMessage(isDuplicate, labelText)
        return genDateInput(labelText, errMessage)
    }
}

data class RawBooleanField(val labelText: String, val trueLabel: String, val falseLabel: String, val isDuplicate: Boolean): RawInputFormBase {
    override fun genHtml(): String {
        return genBooleanInput(isDuplicate, labelText, trueLabel, falseLabel)
    }

    override fun toInputPane(): InputPaneBase {
        return BooleanFieldPane(labelText, trueLabel, falseLabel)
                .apply { showDuplicateError(isDuplicate) }
    }
}

