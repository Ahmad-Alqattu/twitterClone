import com.google.inject.Inject;
import org.example.models.Tweet;
import org.example.models.User;
import org.example.services.TweetService;
import org.example.services.UserService;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.google.inject.Injector;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;

@Testcontainers
public class TweetServiceTest {

    private static PostgreSQLContainer<?> postgresContainer;

    private TweetService tweetService;
    private UserService userService;
    private   Injector injector;
    private  Jdbi jdbi;


    @BeforeEach
    public void setUp() {
        Injector injector = InjectorManager.getInjector();
        tweetService = injector.getInstance(TweetService.class);
        userService = injector.getInstance(UserService.class);
        jdbi = injector.getInstance(Jdbi.class);  // Get Jdbi instance from Guice

    }

    @AfterAll
    static void tearDown() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }
    @AfterEach
    public void deleteUsersTweets() {
        // Clean up users and tables (adjust SQL statements as needed)
        if (jdbi != null) {
            jdbi.useHandle(handle -> {
                handle.execute("TRUNCATE TABLE users CASCADE");
                handle.execute("TRUNCATE TABLE tweets CASCADE"); // Drop your table
            });
        }
    }


    @Test
    public void testCreateTweet() {
        // arrange
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("testuser@example.com");
        newUser.setPasswordHash("password");
        userService.SignUp(newUser);
        User user = userService.findByUsername("testuser");
        int userId = user.getId();

        // act
        String content = "This is a test tweet";
         tweetService.createTweet(userId, content, null);

        // assert
        List<Tweet> tweets = userService.getUserTweets(userId,0,1);
        assertThat(tweets).isNotNull().isNotEmpty();
        assertThat(tweets).hasSize(1);
        assertThat(tweets.get(0).getContent()).isEqualTo(content);
    }
}
