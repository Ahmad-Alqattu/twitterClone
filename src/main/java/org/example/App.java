package org.example;


import io.javalin.rendering.template.JavalinPebble;
import org.example.config.ConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.javalin.Javalin;
import org.example.controllers.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new ConfigModule(), new TwitterModule());
            Config config = injector.getInstance(Config.class);

            logger.info("Configuration loaded: {}", config.toString());


            Javalin app = Javalin.create(javalinConfig -> {
                javalinConfig.staticFiles.add("/public");
                javalinConfig.fileRenderer(new JavalinPebble());
            }).start(config.getInt("server.port"));

            logger.info("Javalin server started on port: {}", config.getInt("server.port"));

            registerRoutes(app, injector);
        } catch (Exception e) {
            logger.error("Exception occurred during application startup"+e.getMessage().toString());
        }
    }

    private static void registerRoutes(Javalin app, Injector injector) {
        injector.getInstance(AuthController.class).registerRoutes(app);
        injector.getInstance(FeedController.class).registerRoutes(app);
        injector.getInstance(TweetController.class).registerRoutes(app);
        injector.getInstance(SearchController.class).registerRoutes(app);
        injector.getInstance(ProfileController.class).registerRoutes(app);
    }
}
