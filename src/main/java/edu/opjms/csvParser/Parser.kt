package edu.opjms.csvParser

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.apache.commons.io.input.CountingInputStream
import java.io.File
import java.text.ParseException

@ExperimentalStdlibApi
class Parser(private val file: File) {

    val headers = csvReader().open(file) {
        val ret = readNext()

        if (ret != null)
            return@open ret
        else
            throw IllegalArgumentException("No headers found")
    }

    private val headerTypes = mutableMapOf<String, DataTypes>()

    var primaryKey: String? = null
    set(value) {

        if (value != null) {  //null not allowed

            if (value in headers) {

                val dataType = headerTypes.getOrPut(value, { DataTypes.INTEGER }) //value in map, if exists, else put integer

                if (dataType != DataTypes.INTEGER)
                    throw IllegalArgumentException("Provided header is not of Integer Data type, it is type: $dataType")
                else
                    field = value

            } else
                throw IllegalArgumentException("provided header: $value was not found it the headers read in file")


        } else
            throw IllegalArgumentException("Nulls not allowed")
    }

    fun setTypeOfHeader(header: String, dataType: DataTypes) {

        if (header in headers) {
            if (header != primaryKey)
                headerTypes[header] = dataType
            else if (dataType != DataTypes.INTEGER) //primary key must be an integer, dont change it
                throw java.lang.IllegalArgumentException("Primary key can not be changed to non - integer")

        } else
            throw IllegalArgumentException("provided header: $header was not found it the headers read in file")
    }

    fun evalData() {
        // all entries in map are headers found in the list,
        // so comparing their lengths is enough to verify that
        // data type for all headers has been set
        if (primaryKey == null)
            throw IllegalStateException("primary key must be initialised")
        if (headers.size != headerTypes.size)
            throw IllegalStateException("data types for all headers are not set")

        //build columnBuilders
        val columns = genColumnBuilders()

        val stream = CountingInputStream(file.inputStream())

        var oldRead = 0L
        var newRead: Long
        val length = file.length()
        csvReader().open(stream) {
            /*
            * Not using read with headers because it returns a map
            * reading thousands of rows this way will
            * put pressure on gc.
            *
            * This is quite readable too
            * */

            //skip header row
            readNext()

            readAllAsSequence().forEachIndexed { index, list ->
                for (i in list.indices) {

                    val err = columns.getValue(headers[i]).evaluate(list[i])

                    if (err != null) {
                        throw ParseException("Invalid Argument at line $index: $err", 0)
                    }

                }
                //progress reporting
                newRead = stream.byteCount
                if (newRead > oldRead) {
                    println("bytes read: $newRead")
                    val progress =  newRead.toDouble() / length * 100
                    println("progress: $progress")
                    oldRead = newRead
                }
            }
        }

        println(columns.toString())
    }

    private fun genColumnBuilders() =
        buildMap<String, ColumnBuilder>(headers.size) {
            headerTypes.forEach {
                if (it.key != primaryKey)
                    put(it.key, it.value.getColumnBuilder(it.key))
                else
                    put(it.key, IntColumnBuilder(primaryKey!!, true))
            }
        }

}

@ExperimentalStdlibApi
fun test() {
    val file = File("D:\\Sqld.txt")
//    val file = File("C:\\Users\\DEL\\Downloads\\student_table.csv")
    val parser = Parser(file)
    println(parser.headers)

    //simulating gui input
    parser.primaryKey = "admno"
    parser.setTypeOfHeader("first_name", DataTypes.STRING)
    parser.setTypeOfHeader("class", DataTypes.INTEGER)
    parser.setTypeOfHeader("section", DataTypes.ENUM)
    parser.setTypeOfHeader("has_voted", DataTypes.BOOLEAN)
    parser.setTypeOfHeader("house", DataTypes.ENUM)
    parser.setTypeOfHeader("date of birth", DataTypes.DATE_YEAR_MONTH_DAY)

    val time = System.currentTimeMillis()
    parser.evalData()
    println("${System.currentTimeMillis() - time}ms")
}


enum class DataTypes {
    STRING,
    INTEGER,
    DOUBLE,
    BOOLEAN,
    ENUM,
    DATE_YEAR_MONTH_DAY,
    DATE_YEAR_DAY_MONTH,
    DATE_MONTH_YEAR_DAY,
    DATE_MONTH_DAY_YEAR,
    DATE_DAY_MONTH_YEAR,
    DATE_DAY_YEAR_MONTH;


    fun getColumnBuilder(name: String): ColumnBuilder = when (this) {
        STRING -> StringColumnBuilder(name)
        INTEGER -> IntColumnBuilder(name)
        DOUBLE -> DoubleColumnBuilder(name)
        BOOLEAN -> BooleanColumnBuilder(name)
        ENUM -> EnumColumnBuilder(name)

        DATE_YEAR_MONTH_DAY, DATE_YEAR_DAY_MONTH,
        DATE_MONTH_YEAR_DAY, DATE_MONTH_DAY_YEAR,
        DATE_DAY_MONTH_YEAR, DATE_DAY_YEAR_MONTH -> DateColumnBuilder(name, this)
    }
}