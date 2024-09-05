import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import static io.javalin.Javalin.create;
import static io.javalin.Javalin.start;
import static io.javalin.Javalin.stop;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AppIntegrationTest {

    private PostgreSQLContainer<?> postgresContainer;

    @BeforeEach
    void setUp() {
        // Start PostgreSQL container
        postgresContainer = new PostgreSQLContainer<>("postgres:13")
                .withDatabaseName("TwitterDB")
                .withUsername("postgres")
                .withPassword("master123");
        postgresContainer.start();

        // Configure your Javalin app to use this container's database URL
        System.setProperty("database.url", postgresContainer.getJdbcUrl());
        System.setProperty("database.username", postgresContainer.getUsername());
        System.setProperty("database.password", postgresContainer.getPassword());

        // Start your Javalin app
        App.main(new String[]{});
    }

    @AfterEach
    void tearDown() {
        // Stop the Javalin app
        stop();

        // Stop PostgreSQL container
        postgresContainer.stop();
    }

    @Test
    void testSomeEndpoint() {
        // Arrange: Prepare data or mock services if needed

        // Act: Make a request to the Javalin app
        // For example, using an HTTP client to make a request
        // HTTPResponse response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        // Assert: Check the response
        // assertEquals(200, response.statusCode());
        // assertEquals("Expected response", response.body());
    }
}
