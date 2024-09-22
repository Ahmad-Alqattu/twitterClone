import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.example.models.User;
import org.example.services.UserService;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class UserServiceTest {

    private UserService userService;
    private static PostgreSQLContainer<?> postgresContainer;
    private static Injector injector;
    private  Jdbi jdbi;


    @BeforeEach
     void setUp() {
        Injector injector = InjectorManager.getInjector();
        userService = injector.getInstance(UserService.class);
        postgresContainer = injector.getInstance(PostgreSQLContainer.class);
    }

    @AfterAll
    static void tearDown() {
        if (postgresContainer != null) {
            postgresContainer.stop();
        }
    }
    @AfterEach
    public void deleteUsers() {
        if (jdbi != null) {
            jdbi.useHandle(handle -> {
                handle.execute("TRUNCATE TABLE users CASCADE"); // Drop your table
            });
        }
    }
    @Test
    void testUserCreation() {
        // arrange
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setEmail("testuser@example.com");
        newUser.setPasswordHash("password");

        //act
        userService.SignUp(newUser);
        
        // assert
        User fetchedUser = userService.findByUsername("testuser");
        assertNotNull(fetchedUser);
        assertEquals("testuser", fetchedUser.getUsername());
        assertEquals("testuser@example.com", fetchedUser.getEmail());
    }

}
