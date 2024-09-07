import org.example.models.Tweet;
import org.example.models.User;
import org.example.services.TweetService;
import org.example.services.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class TweetServiceIntegrationTest {

    private static PostgreSQLContainer<?> postgresContainer;

    private TweetService tweetService;
    private UserService userService;
    private Jdbi jdbi;

    @BeforeEach
    public void setUp() {
        // Create an Injector with a TestModule (you can use the TestModule you've already created)
        Injector injector = Guice.createInjector(new TestModule(),new ConfigModule());


        // Initialize services
        tweetService = injector.getInstance(TweetService.class);
        userService = injector.getInstance(UserService.class);

        // Get the Jdbi instance
        jdbi = injector.getInstance(Jdbi.class);

        // Insert a user into the "users" table
         User newUser = new User();
                newUser.setUsername("testuser");
                newUser.setEmail("testuser@example.com");
                newUser.setPasswordHash("password");
        userService.SignUp(newUser);
    }

    @AfterEach
    public void tearDown() {
        // Clean up after each test
        // Stop the container after all tests
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

    @Test
    public void testCreateTweet() {
        // Given: A user and tweet content
        User user = userService.findByUsername("testuser");
        int userId = user.getId();
        String content = "This is a test tweet";

        // When: Creating a tweet
        Tweet tweet = tweetService.createTweet(userId, content, null);

        // Then: Tweet should be created successfully and can be retrieved from the database
        assertNotNull(tweet);
        assertEquals(userId, tweet.getUserId());
        assertEquals(content, tweet.getContent());

        // Verify that the tweet was inserted in the database
        List<Tweet> tweets = userService.getUserTweets(userId,0,2);

        assertEquals(1, tweets.size());
        assertEquals(content, tweets.get(0).getContent());
    }
}
