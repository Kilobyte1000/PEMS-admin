package net.kilobyte1000

import io.activej.http.GzipProcessorUtils
import org.mariadb.jdbc.MariaDbPoolDataSource
import java.util.zip.GZIPOutputStream

fun startServer(
        dataSource: MariaDbPoolDataSource,
        inputForm: String,
        prefects: Map<Houses, PrefectList>
) {
        setLoginPage(inputForm)

}

fun gzipResources() {

}