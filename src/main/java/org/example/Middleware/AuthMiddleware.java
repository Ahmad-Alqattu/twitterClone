package org.example.Middleware;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.UnauthorizedResponse;

public class AuthMiddleware implements Handler {

    @Override
    public void handle(Context ctx) throws Exception {
        Integer userId = ctx.sessionAttribute("userId");

        if (userId == null) {
            throw new UnauthorizedResponse("You must be logged in to access this resource.");
        }

    }
}
