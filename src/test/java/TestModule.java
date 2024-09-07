import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.example.dao.JdbiTweetDAO;
import org.example.dao.JdbiUserDAO;
import org.example.dao.TweetDAO;
import org.example.dao.UserDAO;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import com.typesafe.config.Config;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(UserDAO.class).to(JdbiUserDAO.class).in(Singleton.class);
        bind(TweetDAO.class).to(JdbiTweetDAO.class).in(Singleton.class);
    }

    @Provides
    @Singleton
    public Jdbi provideJdbi(PostgreSQLContainer<?> postgresContainer) {
        // Apply Flyway migrations to the Testcontainer's DB
        Flyway flyway = Flyway.configure()
                .dataSource(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword())
                .load();
        flyway.migrate();
        return Jdbi.create(postgresContainer.getJdbcUrl(), postgresContainer.getUsername(), postgresContainer.getPassword());
    }

    @Provides
    @Singleton
    public PostgreSQLContainer<?> providePostgresContainer(Config config) {
        PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:latest")
                .withDatabaseName(config.getString("database.dbname"))
                .withUsername(config.getString("database.username"))
                .withPassword(config.getString("database.password"));
        postgresContainer.start();
        return postgresContainer;
    }
}
