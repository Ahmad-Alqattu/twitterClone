import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class addTweetIntegrationTest {

    @Test
    public void userCanAddTweetAndVisitProfile() {
        // open site
        open("http://localhost:7000/signup");
        Faker faker = new Faker();
        String randomUsername = faker.name().username();
        String randomEmail = faker.internet().emailAddress();
        String randomPassword = faker.internet().password();
        String randomtext = faker.harryPotter().toString();

        // signing
        $("#username").setValue(randomUsername);
        $("#email").setValue(randomEmail);
        $("#password").setValue(randomPassword);
        $("#confirmPassword").setValue(randomPassword);
        $("#sigupButton").click();
        open("http://localhost:7000/login");
        // login
        $("#username").setValue(randomUsername);
        $("#password").setValue(randomPassword);
        $("#loginButton").click();

        // add tweet
        $("#tweetBox").setValue(randomtext);
        $("#tweetButton").click();

        // validate tweet
        $(".tweet").shouldHave(text(randomtext));

        // visit profile
        $("#userProfileLink").click();

        // validate tweet in use profile
        $(".tweet").shouldHave(text(randomtext));
    }
}
