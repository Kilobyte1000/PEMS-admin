package net.kilobyte1000

import io.activej.http.*
import io.activej.launchers.http.MultithreadedHttpServerLauncher
import java.util.concurrent.Executors
import java.util.function.Supplier
import net.kilobyte1000.WebApp
import io.activej.inject.annotation.Provides
import kotlin.Throws
import kotlin.jvm.JvmStatic
import io.activej.launcher.Launcher
import java.lang.Exception
import java.util.concurrent.Executor

class WebApp : MultithreadedHttpServerLauncher() {
    @Provides
    fun executor(): Executor {
        System.err.println("ive been created")
        return Executors.newSingleThreadExecutor()
    }

    @Provides
    fun servlet(executor: Executor?): AsyncServlet {
        return RoutingServlet.create()
            .map("/",
                StaticServlet.ofClassPath(executor, "/httpResources/html/")
                    .withIndexResources("loginPage.html")
                    .withContentType(ContentTypes.HTML_UTF_8)
                    .withResponse {
                        HttpResponse.ok200()
                            .withHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VAL)
                            .withBodyGzipCompression()
                    }
            ).map("/httpResources/assets/*",
                StaticServlet.ofClassPath(executor, "/httpResources/assets/")
                    .withIndexResources(
                        "css/login.css",
                        "img/logo.jpg",
                        "js/errShow.js"
                    )
                    .withResponse { HttpResponse.ok200().withHeader(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_VAL) }
            ).map(
                HttpMethod.GET,
                "/vote"
            ) { HttpResponse.redirect302("/") } // GET request on vote page
    }


    fun main() {
        val launcher: Launcher = WebApp()
        launcher.launch(args)
    }

    companion object {
        private const val CACHE_CONTROL_VAL = "public, max-age=5184000, immutable"
    }
}