import com.google.inject.Guice;
import com.google.inject.Injector;
import org.example.dao.UserDAO;
import org.example.models.User;
import org.example.services.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceIntegrationTest {

    private UserService userService;
    private static PostgreSQLContainer<?> postgresContainer;

    @BeforeEach
    void setUp() {
        // Set up Guice injector with TestModule
        Injector injector = Guice.createInjector(new TestModule(),new ConfigModule());
        userService = injector.getInstance(UserService.class);
        postgresContainer = injector.getInstance(PostgreSQLContainer.class);
    }

    @AfterAll
    static void tearDown() {
        // Stop the container after all tests
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }

    @Test
    void testUserCreation() {
        // Test the creation of a user
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("testuser@example.com");
        newUser.setPasswordHash("password");

        userService.SignUp(newUser);

        User fetchedUser = userService.findByUsername("testuser");
        assertNotNull(fetchedUser);
        assertEquals("testuser", fetchedUser.getUsername());
        assertEquals("testuser@example.com", fetchedUser.getEmail());
    }

}
