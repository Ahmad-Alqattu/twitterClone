package org.example;


import io.javalin.rendering.template.JavalinPebble;
import jakarta.servlet.MultipartConfigElement;
import org.example.Middleware.AuthMiddleware;
import org.example.config.ConfigModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.javalin.Javalin;
import org.example.controllers.*;
import org.example.services.AuthService;
import org.example.services.TweetService;
import org.example.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        try {
            Injector injector = Guice.createInjector(new ConfigModule(), new TwitterModule());
            Config config = injector.getInstance(Config.class);
            UserService userService = injector.getInstance(UserService.class);
            TweetService tweetService = injector.getInstance(TweetService.class);
            AuthService authService = injector.getInstance(AuthService.class);
            logger.info("Configuration loaded: {}", config.toString());


            Javalin app = Javalin.create(javalinConfig -> {
                javalinConfig.staticFiles.add("/public");
                javalinConfig.fileRenderer(new JavalinPebble());

            }).start(config.getInt("server.port"));

            // Register authentication filter (middleware) globally
            app.before("/feed/*", new AuthMiddleware());
            app.before("/profile/update", new AuthMiddleware());
            app.before("/profile/{username}/follow", new AuthMiddleware());
            app.before("/profile/{username}/unfollow", new AuthMiddleware());
            app.before("/tweet/*", new AuthMiddleware());

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
