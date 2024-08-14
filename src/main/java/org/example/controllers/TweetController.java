package org.example.controllers;

import org.example.dao.TweetDAO;
import org.example.models.Comment;
import org.example.models.Tweet;
import org.example.models.User;
import com.google.inject.Inject;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static io.javalin.rendering.template.TemplateUtil.model;

public class TweetController {
    private final TweetDAO tweetDAO;

    @Inject
    public TweetController(TweetDAO tweetDAO) {
        this.tweetDAO = tweetDAO;
    }

    public void registerRoutes(Javalin app) {
        app.post("/tweet/{id}/like", this::toggleLike);
        app.get("/tweet/{id}/likers", this::getLikers);

        app.post("/tweet/{id}/retweet", this::toggleRetweet);
        app.get("/tweet/{id}/retweeters", this::getRetweeters);
        app.post("/tweet/{id}/comment", this::addComment);

    }

    public void toggleLike(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        int userId = ctx.sessionAttribute("userId");

        if (tweetDAO.isLikedByMe(tweetId,userId)) {
            tweetDAO.unlike(userId, tweetId);
        } else {
            tweetDAO.like(userId, tweetId);
        }

        Tweet tweet = tweetDAO.getTweetById(tweetId,userId);
        ctx.render("./templates/partials/like_button.peb", Map.of("tweet", tweet));
    }

    public void getLikers(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        List<User> likers = tweetDAO.likers(tweetId);
        ctx.render("./templates/partials/reactors.peb", Map.of("reactors", likers));
    }

    public void toggleRetweet(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        int userId = ctx.sessionAttribute("userId");
        Tweet tweet = tweetDAO.getTweetById(tweetId,userId);

        if (tweetDAO.isRetweetedByMe(tweetId,userId)) {
            tweetDAO.unretweet(userId, tweetId);
        } else {
            tweetDAO.retweet(userId, tweetId);
        }

        tweet = tweetDAO.getTweetById(tweetId,userId);
        ctx.render("./templates/partials/retweet_button.peb", Map.of("tweet", tweet));
    }
    private void addComment(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        String content = ctx.formParam("content");
        Integer userId = ctx.sessionAttribute("userId");

        if (userId == null) {
            ctx.status(401).result("User not authenticated");
            return;
        }

        Comment comment = tweetDAO.addComment(tweetId, userId, content);
        ctx.contentType("text/html");
        ctx.result(renderCommentHtml(comment));
    }

    private String renderCommentHtml(Comment comment) {
        return String.format(
                "<div class=\"comment mb-2 d-flex align-items-center\">" +
                        "<img src=\"%s\" class=\"rounded-circle me-2\" width=\"30\" height=\"30\" alt=\"%s\">" +
                        "<p class=\"mb-0\"><strong>%s:</strong> %s</p>" +
                        "</div>"+
                "    <form hx-post=\"/tweet/{{ tweet.id }}/comment\" hx-swap=\"delete\">\n" +
                        "        <input type=\"text\" name=\"content\" class=\"form-control mb-2\" placeholder=\"Add a comment\" required>\n" +
                        "        <button type=\"submit\" class=\"btn btn-sm btn-primary\" >Comment</button>\n" +
                        "    </form>",
                comment.getUser().getProfilePicData(),
                comment.getUser().getUsername(),
                comment.getUser().getUsername(),
                comment.getContent()
        );
    }
    public void getRetweeters(Context ctx) {
        int tweetId = Integer.parseInt(ctx.pathParam("id"));
        List<User> retweeters = tweetDAO.retweeters(tweetId);
        ctx.render("templates/partials/reactors.peb", Map.of("reactors", retweeters));
    }

    private void createPost(Context ctx) {
        int userId = Integer.parseInt(ctx.formParam("user_id"));
        String content = ctx.formParam("content");
        String imageUrl = ctx.formParam("image_url");

        ctx.redirect("/posts");
    }
}