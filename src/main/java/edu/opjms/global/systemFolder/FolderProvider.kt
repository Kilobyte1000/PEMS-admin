package edu.opjms.global.systemFolder

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.Month

val dataFolder: Path = Paths.get("D:\\images")

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