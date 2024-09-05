package org.example.controllers;

import org.example.models.User;
import org.example.services.AuthService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;

import static io.javalin.rendering.template.TemplateUtil.model;

public class AuthController {
    private final AuthService authService;

    @Inject
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/login", this::renderLoginPage);
        app.post("/login", this::handleLogin);
        app.get("/signup", this::renderSignupPage);
        app.post("/signup", this::handleSignup);
        app.get("/logout", this::handleLogout);
    }

    private void renderLoginPage(Context ctx) {
        ctx.render("templates/login.peb", new HashMap<>());
    }

    private void handleLogin(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        User user = authService.login(username, password);
        if (user != null) {
            ctx.sessionAttribute("userId", user.getId());
            ctx.redirect("/feed");
        } else {
            ctx.render("templates/login.peb", model(
                    "isThereErrors", true,
                    "errorMessage", "Invalid username or password"
            ));
        }
    }

    private void renderSignupPage(Context ctx) {
        ctx.render("templates/signup.peb", new HashMap<>());
    }

    private void handleSignup(Context ctx) {
        String username = ctx.formParam("username");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        if (authService.isUsernameTaken(username)) {
            ctx.render("templates/signup.peb", model(
                    "isThereErrors", true,
                    "errorMessage", "Username already exists",
                    "email", email
            ));
            return;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(password);  // This will be hashed in the service

        authService.signUp(newUser);
        ctx.sessionAttribute("userId", newUser.getId());
        ctx.redirect("/feed");
    }

    private void handleLogout(Context ctx) {
        ctx.sessionAttribute("userId", null);
        ctx.redirect("/login");
    }
}
