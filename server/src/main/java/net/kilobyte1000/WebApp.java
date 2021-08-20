package net.kilobyte1000;

import io.activej.http.*;
import io.activej.inject.annotation.Provides;
import io.activej.launcher.Launcher;
import io.activej.launchers.http.MultithreadedHttpServerLauncher;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static io.activej.http.HttpHeaders.CACHE_CONTROL;

public class WebApp extends MultithreadedHttpServerLauncher {

    private final static String CACHE_CONTROL_VAL = "public, max-age=5184000, immutable";



    @Provides
    Executor executor() {
        System.err.println("ive been created");
        return Executors.newSingleThreadExecutor();
    }



    @Provides
    AsyncServlet servlet(Executor executor) {
        return RoutingServlet.create()
                .map("/",
                        StaticServlet.ofClassPath(executor, "/httpResources/html/")
                            .withIndexResources("loginPage.html")
                            .withContentType(ContentTypes.HTML_UTF_8)
                            .withResponse(() -> HttpResponse.ok200()
                                .withHeader(CACHE_CONTROL, CACHE_CONTROL_VAL)
                                .withBodyGzipCompression())

                ).map("/httpResources/assets/*",
                        StaticServlet.ofClassPath(executor, "/httpResources/assets/")
                            .withIndexResources("css/login.css",
                                    "img/logo.jpg",
                                    "js/errShow.js")
                            .withResponse(() -> HttpResponse.ok200().withHeader(CACHE_CONTROL, CACHE_CONTROL_VAL))
                ).map(HttpMethod.GET, "/vote", httpRequest -> HttpResponse.redirect302("/")) // GET request on vote page
                ;

    }



    public static void main(String[] args) throws Exception {
        Launcher launcher = new WebApp();
        launcher.launch(args);
    }
}
