@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package edu.opjms.csvParser

import org.apache.commons.lang3.StringUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import java.lang.Long as JLong

sealed class ColumnBuilder(val name: String) {
    abstract fun evaluate(input: String): String?
    abstract fun genColumnDef(): String
    protected val dbName = encodeColumnName(name)
}

class IntColumnBuilder(name: String, private val isPrimaryKey: Boolean = false) : ColumnBuilder(name) {

    var min = Long.MAX_VALUE
        private set
    var max = Long.MIN_VALUE
        private set

    var isUnsigned = true
    var isAssigned = false

    @Throws(NumberFormatException::class)
    override fun evaluate(input: String): String? {
        //if it was unsigned before now
        try {
            if (isUnsigned) {
                //if this input >= 0
                if (input[0] != '-') {
                    val uLong = JLong.parseUnsignedLong(input)

                    if (isAssigned) {
                        if (JLong.compareUnsigned(uLong, min) < 0)
                            min = uLong
                        if (JLong.compareUnsigned(uLong, max) > 0)
                            max = uLong
                    } else {
                        //can't rely on default min-max as max is negative
                        min = uLong
                        max = uLong
                        isAssigned = true
                    }

                } else {
                    val value = input.toLong()
                    if (isAssigned) {
                        // negative encountered, convert max from unsigned
                        // to signed, if possible
                        if (JLong.compareUnsigned(max, JLong.MAX_VALUE) <= 0) {

                            // max has same representation in unsigned
                            // if it is less than Long.MAX_VALUE
                            // no need for any change
                            // just set min accordingly
                            min = value
                        } else throw NumberFormatException("value $max is to big for signed long")
                    } else {
                        min = value
                        max = value
                        isAssigned = true
                    }

                    isUnsigned = false

                }
            } else {
                val value = input.toLong()
                min = min(min, value)
                max = max(max, value)
            }

        } catch (e: NumberFormatException) {
            val unsignedLongMax = "18446744073709551615"
            return if (isUnsigned) // either input is not unsigned long or negative
                "$input is not an integer in range 0 to $unsignedLongMax"
            else //either input is not long or failed to convert unsigned long signed long
                "$input is not an integer in range ${JLong.MIN_VALUE} to ${JLong.MAX_VALUE}"
        }
        return null;
    }

    override fun genColumnDef(): String {
        val isUnsigned = min >= 0

        // the bound that is on more extreme side of
        // range limit and will be used in calculation
        val bound = when {
            isUnsigned -> JLong.divideUnsigned(max - 1,2)
            abs(min) < max -> max
            else -> abs(min) - 1
        }

        val dataType: String = when {
            bound <= 127 -> "tinyint"
            bound in 128..32767 -> "smallint"
            bound in 32768..8388607 -> "mediumint"
            bound in 8388608..2147483647 -> "int"
            bound in 2147483648..9223372036854775807 -> "bigint"
            else -> throw AssertionError(ERR_MESSAGE)
        }

        //10 is for spaces + non null-length
        val size = dbName.length + dataType.length + 10 + if (isUnsigned) 9 else 0

        return buildString(size) {
            append("$dbName $dataType NOT NULL")
            if (isUnsigned) append(" UNSIGNED")
        }
    }

    override fun toString(): String {
        return "IntColumnBuilder(isPrimaryKey=$isPrimaryKey, min=$min, max=$max, isUnsigned=$isUnsigned, isAssigned=$isAssigned)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IntColumnBuilder

        if (isPrimaryKey != other.isPrimaryKey) return false
        if (min != other.min) return false
        if (max != other.max) return false
        if (isUnsigned != other.isUnsigned) return false
        if (isAssigned != other.isAssigned) return false

        return true
    }

    override fun hashCode(): Int {
        var result = isPrimaryKey.hashCode()
        result = 31 * result + min.hashCode()
        result = 31 * result + max.hashCode()
        result = 31 * result + isUnsigned.hashCode()
        result = 31 * result + isAssigned.hashCode()
        return result
    }

    companion object {
        const val ERR_MESSAGE= "Maths for calculation of smallest size of number has inexplicably failed"
    }
}

class StringColumnBuilder(name: String) : ColumnBuilder(name) {
    var maxLength = -1
        private set
    var minLength = Int.MAX_VALUE
        private set

    override fun evaluate(input: String): String? {
        maxLength = max(maxLength, input.length)
        minLength = min(minLength, input.length)
        return null
    }

    override fun genColumnDef(): String {
        if (maxLength > 0) {
            val dataType = if (minLength != maxLength) "varchar" else "char"
            return "$dbName $dataType($maxLength) NOT NULL"
        }
        return ""
    }

    override fun toString(): String {
        return "StringColumnBuilder(maxLength=$maxLength, minLength=$minLength)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StringColumnBuilder

        if (maxLength != other.maxLength) return false
        if (minLength != other.minLength) return false

        return true
    }

    override fun hashCode(): Int {
        var result = maxLength
        result = 31 * result + minLength
        return result
    }

}

@Suppress("EqualsOrHashCode")
class BooleanColumnBuilder(name: String) : ColumnBuilder(name) {
    @Throws(IllegalArgumentException::class)
    override fun evaluate(input: String): String? {
        //nothing to evaluate, just check if input is boolean
        return if (input != "0" && input != "1"
                && !input.equals("true", true)
                && !input.equals("false", true)) {
            "$input should be either 'true' / 'false' / '0' / '1'"
        } else
            null
    }

    override fun genColumnDef(): String {
        return "$dbName BIT(1) NOT NULL"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return super.equals(other)
    }
}

class EnumColumnBuilder(name: String) : ColumnBuilder(name) {
    val options = mutableListOf<String>()

    override fun evaluate(input: String): String? {
        val value = input.toLowerCase()
        if (value !in options)
            options.add(value)
        return null
    }

    override fun genColumnDef(): String {
        return buildString {
            append("$dbName ENUM(")
            for (value in options) {
                append(encodeWithQuote(value))
            }
            append(") NOT NULL")
        }
    }

    override fun toString(): String {
        return "EnumColumnBuilder(options=$options)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EnumColumnBuilder

        if (options != other.options) return false

        return true
    }

    override fun hashCode(): Int {
        return options.hashCode()
    }
}

class DoubleColumnBuilder(name: String) : ColumnBuilder(name) {

    //it is possible that that these may actually
    //all be integral values with declaring these as doubles
    //was a mistake
    //so, we parse them the same way we do with integers
    //until something that does not fit comes
    private var intEvaluator: IntColumnBuilder? = IntColumnBuilder(name)

    @Throws(NumberFormatException::class)
    override fun evaluate(input: String): String? {
        when {
            intEvaluator != null -> {
                try {
                    intEvaluator?.evaluate(input)
                } catch (e: NumberFormatException) {
                    //cant be represented as long
                    intEvaluator = null
                    if (input.toDoubleOrNull() == null)
                        return "$input is not a double"
                }
            }
            input.toDoubleOrNull() == null -> return "$input is not a double"
            else -> return null
        }
        return null
    }

    override fun genColumnDef(): String {
        if (intEvaluator != null)
            return intEvaluator!!.genColumnDef()
        return "$dbName DOUBLE NOT NULL,"
    }

    override fun toString(): String {
        return "DoubleColumnBuilder(intEvaluator=$intEvaluator)"
    }


}

class DateColumnBuilder(name: String, private val dateType: DataTypes) : ColumnBuilder(name) {
    var minDate: Date = Date(Long.MAX_VALUE)
        private set
    var maxDate: Date = Date(Long.MIN_VALUE)
        private set

    private lateinit var formatter: SimpleDateFormat

    override fun evaluate(input: String): String? {

        try {
            if (this::formatter.isInitialized) {
                val date = formatter.parse(input)

                minDate = minOf(minDate, date)
                maxDate = maxOf(maxDate, date)

            } else {
                val format = initFormatter(input)
                formatter = SimpleDateFormat(format)
            }

        } catch (e: ParseException) {
            return "$input is not a valid date"
        } catch (e: IllegalArgumentException) {
            return "$input is not a valid date"
        }
        return null
    }

    override fun genColumnDef(): String {
        return "$dbName DATE NOT NULL"
    }

    @Suppress("NON_EXHAUSTIVE_WHEN")
    private fun initFormatter(input: String): String {
        var i = 0
        val dateFormat = StringBuilder()

        var isMonthInNumber = true

        when (dateType) {

            //if start with year, check if 2-digit or 3-digit
            DataTypes.DATE_YEAR_DAY_MONTH, DataTypes.DATE_YEAR_MONTH_DAY -> {
                i = getFirstNonDigitIndex(input)
                //it is meaningless to have 3 digits in year
                if (i == 3 || i == 0)
                    throw IllegalArgumentException("Year must not have 3 digits")

                // 2 or 1 digit == 'yy' >= 4 digits = 'yyyy'
                dateFormat.append('y').repeat(i.coerceIn(2, 4))
            }

            //if start with date, just move index forwards
            DataTypes.DATE_DAY_MONTH_YEAR, DataTypes.DATE_DAY_YEAR_MONTH -> {
                i = getFirstNonDigitIndex(input)
                if (i == 0)
                    throw IllegalArgumentException("day must have at least 1 digit")
                dateFormat.append("dd")
            }

            DataTypes.DATE_MONTH_DAY_YEAR, DataTypes.DATE_MONTH_YEAR_DAY -> {
                val ch = input[0]
                if (ch.isDigit()) { //just move index ahead
                    i = getFirstNonDigitIndex(input)
                    dateFormat.append("MM")
                } else if (ch.isLetter()) {
                    //assume first non-letter character to be
                    //part of delimiter
                    for (c in input) {
                        if (c.isLetter())
                            i++
                        else
                            break
                    }
                    dateFormat.append("MMMM")

                    isMonthInNumber = false
                }
            }

            else -> throw IllegalStateException("A date was expected, found: $dateType, dummy")
        }

        /*
        * We assume that delimiter does not constitute of digits or letters
        * */
        i = appendDelimiter(dateFormat, input, i)

        when (dateType) {
            //if it is day
            DataTypes.DATE_YEAR_DAY_MONTH, DataTypes.DATE_MONTH_DAY_YEAR -> {
                val j = getFirstNonDigitIndex(input, i)
                if (j == i)
                    throw IllegalArgumentException("day must have at least 1 digit")
                dateFormat.append("dd")
                i = j
            }

            //if it is year
            DataTypes.DATE_MONTH_YEAR_DAY, DataTypes.DATE_DAY_YEAR_MONTH -> {
                val j = getFirstNonDigitIndex(input, i)
                val chars = j - i
                if (chars == 0 || chars == 3)
                    throw IllegalArgumentException("year must not have 3 digits")
                dateFormat.append(if (chars == 2) "yy" else "yyyy")
            }

            //if month
            DataTypes.DATE_DAY_MONTH_YEAR, DataTypes.DATE_YEAR_MONTH_DAY -> {
                val ch = input[i]
                if (ch.isDigit()) { //just move index ahead
                    i = getFirstNonDigitIndex(input, i)
                    dateFormat.append("MM")
                } else if (ch.isLetter()) {
                    //assume first non-letter character to be
                    //part of delimiter
                    for (c in input) {
                        if (c.isLetter())
                            i++
                        else
                            break
                    }
                    dateFormat.append("MMMM")
                    isMonthInNumber = false
                }
            }
        }

        //again delimiter
        i = appendDelimiter(dateFormat, input, i)

        when (dateType) {
            //it is day
            DataTypes.DATE_YEAR_MONTH_DAY, DataTypes.DATE_MONTH_YEAR_DAY -> dateFormat.append("dd")

            //it is month
            DataTypes.DATE_YEAR_DAY_MONTH, DataTypes.DATE_DAY_YEAR_MONTH -> {
                if (input[i].isDigit())
                    dateFormat.append("MM")
                else {
                    isMonthInNumber = false
                    dateFormat.append("MMMM")
                }

            }

            //if it is year
            DataTypes.DATE_DAY_MONTH_YEAR, DataTypes.DATE_MONTH_DAY_YEAR -> {
                val endIndex = getFirstNonDigitIndex(input, i)
                val diff = if (endIndex == -1) input.length - i
                    else endIndex - i

                if (diff == 2)
                    dateFormat.append("yy")
                else
                    dateFormat.append("yyyy")
            }
        }
        return dateFormat.toString()
    }

    private fun appendDelimiter(builder: StringBuilder, string: String, offset: Int): Int {
        val endIndex = firstDigitOrLetterIndex(string, offset)
        builder.append('\'').append(StringUtils.replace(
                string.substring(offset, endIndex),
                "'",
                "''"
        )).append('\'')
        return endIndex
    }

    /**
     * Get the index of first non-digit character, starting the search from the offset as the index
     * */
    private fun getFirstNonDigitIndex(string: String, offset: Int = 0): Int {
        for (i in offset until string.length) {
            if (!string[i].isDigit())
                return i
        }
        return -1
    }

    private fun firstDigitOrLetterIndex(string: String, offset: Int): Int {
        for (i in offset until string.length) {
            if (string[i].isLetterOrDigit())
                return i
        }
        return -1
    }

    override fun toString(): String {
        val minDate = formatter.format(this.minDate)
        val maxDate = formatter.format(this.maxDate)
        return "DateColumnBuilder(minDate=$minDate, maxDate=$maxDate, formatter=$formatter)"
    }
}

private fun encodeColumnName(string: String) = "`" + StringUtils.replace(string, "`", "``") + "`"
private fun encodeWithQuote(string: String) = "'" + StringUtils.replace(string, "'", "''") + "'"

