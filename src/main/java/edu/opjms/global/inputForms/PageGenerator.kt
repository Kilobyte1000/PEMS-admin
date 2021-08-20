package edu.opjms.global.inputForms

import org.apache.commons.text.StringEscapeUtils.escapeHtml4

typealias StringPairArray = Array<Pair<String, String>>


fun genErrButton(errMessage: String) = "<button onClick='toggleVisible(event)' class='err'>Show Errors</button><div class='errBox'>$errMessage</div>"



fun genTextInput(
        labelText: String,
        placeholderText: String,
        typeText: String,
        regex: String?,
        tooltip: String?,
        errMessage: String?
) = buildString {

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


 /*   Functions for generation be Select Fields   */

fun genSelectLabel(labelText: String, containsError: Boolean): String {
    val escapedLabelText = escapeHtml4(labelText)
    val classText = if (containsError) " class='err'" else ""
    return "<label for='$escapedLabelText'$classText>$escapedLabelText</label>"
}

fun genSelectOptions(options: StringPairArray, id: String): String {
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

fun genSelectInput(labelText: String, options: StringPairArray, errMessage: String?): String {
    val containsError = errMessage != null
    val label = genSelectLabel(labelText, containsError)
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

fun genSelectErrMessage(isDuplicate: Boolean, labelText: String, duplicates: List<String>): String? {
    return if (isDuplicate || duplicates.isNotEmpty()) {
        buildString {
            append("<ul>")
            if (isDuplicate)
                append("<li>Duplicate field: '$labelText'</li>")
            for (item in duplicates) {
                append("<li>Duplicate Option: '$item'</li>")
            }
            append("</ul>")
        }
    } else
        null
}
