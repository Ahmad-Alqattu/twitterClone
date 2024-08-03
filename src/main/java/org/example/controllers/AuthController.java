package org.example.controllers;

import org.example.dao.UserDAO;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import org.mindrot.jbcrypt.BCrypt;


import io.javalin.http.Context;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class AuthController {
    private final UserDAO userDAO;

    @Inject
    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void registerRoutes(Javalin app) {
        app.get("/login", this::renderLoginPage);
        app.post("/login", this::handleLogin);
        app.get("/signup", this::renderSignupPage);
        app.post("/signup", this::handleSignup);
        app.post("/logout", this::handleLogout);
    }

    private void renderLoginPage(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        ctx.render("templates/login.peb", model);
    }

    private void handleLogin(Context ctx) {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        User user = userDAO.findByUsername(username);
        if (user != null && BCrypt.checkpw(password, user.getPasswordHash())) {
            ctx.sessionAttribute("userId", user.getId());
            ctx.header("HX-Redirect", "/feed");
        } else {
            ctx.status(401);
            ctx.html("<div class='alert alert-danger'>Invalid username or password</div>");
            ctx.render("path to the sign up form / any pebble page",
                    model("isThereErrors",true,
                            "listOfTweets", new LinkedList<String>()));
        }
    }

    private void renderSignupPage(Context ctx) {
        Map<String, Object> model = new HashMap<>();
        ctx.render("templates/signup.peb", model);


    }

    private void handleSignup(Context ctx) {
        String username = ctx.formParam("username");
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        if (userDAO.findByUsername(username) != null) {
            ctx.status(400);  // Set status to indicate a client error
            ctx.html("<div class='alert alert-danger' hx-swap-oob='true'>Username already exists</div>");
            System.out.println("d");

            return;
        }
        System.out.println("s");

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));

        userDAO.create(newUser);

        ctx.sessionAttribute("userId", newUser.getId());
        ctx.header("HX-Redirect", "/feed");
        ctx.result("Redirecting...");
    }

    private void handleLogout(Context ctx) {
        ctx.sessionAttribute("userId", null);
        ctx.header("HX-Redirect", "/login");
    }
}