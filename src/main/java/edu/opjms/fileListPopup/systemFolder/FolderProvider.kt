package edu.opjms.fileListPopup.systemFolder

import java.io.File
import java.time.LocalDate
import java.time.Month

fun getDataFolder() = File("D:\\images")

fun getCurrentSession(): Int {

    /*
    * session starts in April, So we assume if it is being checked after April,
    * we want the folder of this year
    */

    val date = LocalDate.now()
    return if (date.monthValue < Month.MAY.value) {
        date.year - 1
    } else
        date.year

}

fun sessionFolder(year: Int, parent: File) = parent.resolve("${year}-${year+1}")