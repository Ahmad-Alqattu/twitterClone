package org.example.controllers;

import org.example.models.User;
import org.example.services.UserService;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class AuthController {
    private final UserService userService;

    @Inject
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    public void registerRoutes(Javalin app) {
        app.get("/login", this::renderLoginPage);
        app.post("/login", this::handleLogin);
        app.get("/signup", this::renderSignupPage);
        app.post("/signup", this::handleSignup);
        app.get("/logout", this::handleLogout);
    }

    private void renderLoginPage(Context ctx) {
        ctx.render("./templates/login.peb", new HashMap<>());
    }

    private void handleLogin(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        User user = userService.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
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

        if (userService.findByUsername(username) != null) {
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
        newUser.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));

        userService.updateUserProfile(newUser);
        ctx.sessionAttribute("userId", newUser.getId());
        ctx.redirect("/feed");
    }

    private void handleLogout(Context ctx) {
        ctx.sessionAttribute("userId", null);
        ctx.redirect("/login");
    }
}
