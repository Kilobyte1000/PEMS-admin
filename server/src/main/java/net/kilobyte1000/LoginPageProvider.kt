package net.kilobyte1000

import java.nio.file.*
import kotlin.io.path.*

fun loadLoginPageTest(inputs: String): Path {
    val html = StringBuilder(getResourceAsPath("/httpResources/html/loginPageSample.txt").readText())

    //we have to extract resources to external files
    val cssPath = Files.createTempFile("style", ".css").run {
        Files.copy(getResourceAsPath("/httpResources/assets/css/login.css"), this, StandardCopyOption.REPLACE_EXISTING)
        this.writeBytes(getResourceAsPath("/httpResources/assets/css/err.css").readBytes(), StandardOpenOption.APPEND)
        this.toFile().deleteOnExit()
        this.toAbsolutePath().toString()
    }

    val imgPath = Files.createTempFile("logo", ".jpg").run {
        Files.copy(getResourceAsPath("/httpResources/assets/img/logo.jpg"), this, StandardCopyOption.REPLACE_EXISTING)
        this.toFile().deleteOnExit()
        this.toAbsolutePath().toString()
    }

    val js = getResourceAsPath("/httpResources/assets/js/errDisplay.txt").readText()

    html.replaceFirst("<!--cssSrc-->", "file:///$cssPath")
    html.replaceFirst("<!--imgSrc-->", "file:///$imgPath")
    html.replaceFirst("<!--testJs-->", js)
    html.replaceFirst("<!--insert disabled-->", " disabled")
    html.replaceFirst("<!--inputs-->", inputs)

    val htmlFile = Files.createTempFile("sample", ".html")
    htmlFile.writeText(html.toString())
    htmlFile.toFile().deleteOnExit()
    return htmlFile
}

internal fun setLoginPage(inputs: String) {
    val html = StringBuilder(getResourceAsPath("/httpResources/html/loginPageSample.txt").readText())

    html.replaceFirst("<!--cssSrc-->", "/httpResources/assets/css/login.css")
    html.replaceFirst("<!--imgSrc-->", "/httpResources/assets/img/logo.jpg")
    html.replaceFirst("<!--testJs-->", "")
    html.replaceFirst("<!--insert disabled-->", "")
    html.replaceFirst("<!--inputs-->", inputs)

    getResourceAsPath("/httpResources/html/loginPage.html").writeText(html.toString())
}

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
private fun getResourceAsPath(resource: String)
    = Paths.get(WebApp::class.java.getResource(resource).toURI())

/**
* Please note this thrown an exception if the search string does not exist
* */
private fun StringBuilder.replaceFirst(search: String, replacement: String): StringBuilder {
    val i = indexOf(search)
    return replace(i, i + search.length, replacement)
}