package edu.opjms.global.inputForms

import edu.opjms.templating.RawTypes
import org.apache.commons.text.StringEscapeUtils.escapeHtml4

typealias StringPairList = List<Pair<String, String>>





/****************************************************************
 *                                                              *
 *  Text Input related HTML generation                          *
 *                                                              *
 ****************************************************************/
fun genTextInput(
        labelText_: String,
        placeholderText_: String,
        type: RawTypes?,
        regex_: String?,
        tooltip_: String?,
        errMessage: String?
) = buildString {
    val labelText = escapeHtml4(labelText_)
    val placeholderText = escapeHtml4(placeholderText_)
    val typeText = escapeHtml4(type?.toString() ?: "")
    val regex = escapeHtml4(regex_)
    val tooltip = escapeHtml4(tooltip_)
    val containsError = errMessage != null

    append("<div class='container")
    if (containsError)
        append(" err")

    //the label
    append("'><div class='label'><label ")
    if (containsError)
        append("class='err' ")
    append("for='$labelText'>$labelText</label>")

    //error box
    if (containsError)
        append(genErrButton(errMessage!!))

    //input
    append("</div><div class='input-text-field'><input id='$labelText'" +
            "type='$typeText' placeholder=' ' autocomplete='off'")
    if (!tooltip.isNullOrBlank())
        append(" title='$tooltip'")
    if (!regex.isNullOrBlank())
        append(" pattern='$regex'")

    //placeholder label
    append(" required><label for='$labelText' class='tooltip'>$placeholderText" +
            //close label, add underline, close div's
            "</label><div class='underline'></div></div></div>")
}


fun genTextErrMessage(
        isLabelBlank: Boolean,
        isLabelDuplicate: Boolean,
        isPlaceholderBlank: Boolean,
        isTypeUnselected: Boolean,
        isRegexInvalid: Boolean,
        regex: String,
        labelText: String
): String? {
    return if (isLabelBlank || isLabelDuplicate || isPlaceholderBlank || isTypeUnselected || isRegexInvalid) {
        buildString {
            this append "<ul>"
            if (isLabelBlank)
                this append li("Label Name is not provided")
            if (isPlaceholderBlank)
                this append li("Placeholder is not provided")
            if (isTypeUnselected)
                this append li("Type is not provided")
            if (isRegexInvalid)
                this append li("Provided regex: ${escapeHtml4(regex)} is invalid")
            if (isLabelDuplicate)
                this append  li("Duplicate Field: ${escapeHtml4(labelText)}")
        }
    } else null
}

/****************************************************************
 *                                                              *
 *  Select field Input related HTML generation                  *
 *                                                              *
 ****************************************************************/

private fun genSelectLabel(labelText: String, containsError: Boolean): String {
//    val escapedLabelText = escapeHtml4(labelText)
    val classText = if (containsError) " class='err'" else ""
    return "<label for='$labelText'$classText>$labelText</label>"
}

private fun genSelectOptions(options: StringPairList, id: String): String {
    val escapedId = escapeHtml4(id)

    return buildString {
        append("<select id='$escapedId'><option value='' selected>-</option>")
        for (pair in options) {
            append("<option value='${escapeHtml4(pair.second)}'>")
            append("${escapeHtml4(pair.first)}</option>")
        }
        append("</select>")
    }
}

fun genSelectInput(labelText: String, options: StringPairList, errMessage: String?): String {
    val containsError = errMessage != null
    val label = genSelectLabel(escapeHtml4(labelText), containsError)
    val optionText = genSelectOptions(options, labelText)

    return buildString {
        append("<div class='container")
        if (containsError) {
            append(" err")
        }
        append("'><div class='label'>$label")

        if (containsError)
            append(genErrButton(errMessage!!))

        append("</div><div class='input-field'>$optionText</div></div>")
    }
}

fun genSelectErrMessage(isDuplicate: Boolean, labelText: String, duplicates: Collection<String>, nonNumerics: Collection<String>): String? {
    val isBlank = labelText.isBlank()
    return if (isDuplicate || duplicates.isNotEmpty() || nonNumerics.isNotEmpty() || isBlank) {
        buildString {
            append("<ul>")
            if (isBlank)
                this append "Label Name is not provided"
            if (isDuplicate)
                this append li("Duplicate field: '${escapeHtml4(labelText)}'")
            for (item in duplicates) {
                this append li("Duplicate Option: '${escapeHtml4(item)}'")
            }
            for (item in nonNumerics) {
                this append li("Option is not Numeric: '${escapeHtml4(item)}'")
            }
            append("</ul>")
        }
    } else
        null
}

/****************************************************************
 *                                                              *
 *  Date field Input related HTML generation                    *
 *                                                              *
 ****************************************************************/

fun genDateErrMessage(isDuplicate: Boolean, labelText: String): String? {
    val isLabelBlank = labelText.isBlank()
    return if (isDuplicate || isLabelBlank) {
        buildString {
            this append "<ul>"
            if (isDuplicate)
                this append li("Duplicate Field: ${escapeHtml4(labelText)}")
            if (isLabelBlank)
                this append li("Label Name is not provided")
            this append "</ul>"
        }
    } else null
}

fun genDateInput(labelText_: String, errMessage: String?): String {
    val labelText = escapeHtml4(labelText_)
    val dbName = escapeHtml4(labelText.substring(0, 3).trim())
    val fieldID = dbName + "_field"
    val hasError = errMessage != null

    return "<div class='container${if (hasError) " err" else ""}'>" +
        "<div class='label'>" +
            "<label ${if (hasError) "class='err' " else ""}for='$fieldID'>$labelText</label>" +
            if (errMessage != null) genErrButton(errMessage) else "" +
        "</div>" +
        "<div class='input-text-field date'>" +
            "<input type='date' name='$dbName' id='$fieldID' required>" +
            "<div class='underline'></div>" +
        "</div>" +
    "</div>"
}

/****************************************************************
 *                                                              *
 *  Boolean field Input related HTML generation                 *
 *                                                              *
 ****************************************************************/
fun genBooleanInput(isDuplicate: Boolean, labelText_: String, trueLabel: String, falseLabel: String): String {
    val labelText = escapeHtml4(labelText_)

    val errMessage = run {
        val isLabelBlank = labelText_.isBlank()
        val isTrueBlank = trueLabel.isBlank()
        val isFalseBlank = falseLabel.isBlank()
        val isSame = trueLabel.equals(falseLabel, true)

        if (isDuplicate || isLabelBlank || isTrueBlank || isFalseBlank || isSame) {
            buildString {
                this append "<ul>"
                if (isDuplicate)
                    this append li("Duplicate Field: $labelText")
                if (isLabelBlank)
                    this append li("Label Name is not provided")
                if (isTrueBlank)
                    this append li("True Label is not provided")
                if (isFalseBlank)
                    this append li("False Label is not provided")
                if (isSame)
                    this append li("True and False vales must have distinct labels")
            }
        } else null
    }
    val hasError = errMessage != null

    return "<div class='container${if (hasError) " err" else ""}'>" +
        "<div class='label'>" +
            genSelectLabel(labelText, hasError) +
            (if (hasError) genErrButton(errMessage!!) else "") +
        "</div>" +
        "<div class='input-field'>" +
            genSelectOptions(listOf(trueLabel to "true", falseLabel to "false"), labelText) +
        "</div>" +
    "</div>"
}


//helper functions

@Suppress("NOTHING_TO_INLINE")
private inline fun li(text: String) = "<li>$text</li>"

@Suppress("NOTHING_TO_INLINE")
private inline infix fun StringBuilder.append(str: String) = append(str)

private fun genErrButton(errMessage: String) = "<button onClick='toggleVisible(event)' class='err'>Show Errors</button><div class='errBox'>$errMessage</div>"