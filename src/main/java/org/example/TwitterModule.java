    package org.example;
    import org.example.dao.JdbiTweetDAO;
    import org.example.dao.JdbiUserDAO;
    import org.example.dao.TweetDAO;
    import org.example.dao.UserDAO;
    import com.google.inject.AbstractModule;
    import com.google.inject.Provides;
    import com.google.inject.Singleton;
    import com.typesafe.config.Config;
    import com.typesafe.config.ConfigFactory;
    import org.flywaydb.core.Flyway;
    import org.jdbi.v3.core.Jdbi;


    public class TwitterModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(UserDAO.class).to(JdbiUserDAO.class).in(Singleton.class);
            bind(TweetDAO.class).to(JdbiTweetDAO.class).in(Singleton.class);
            // Add other bindings as needed

        }

        @Provides
        @Singleton
        public Jdbi provideJdbi(Config config) {

            Flyway flyway = Flyway.configure()
                    .dataSource(config.getString("database.url"),  config.getString("database.username"),  config.getString("database.password"))
                    .locations("db/migration")
                    .table("flyway_schema_history")
                    .load();

            flyway.migrate();

            return Jdbi.create(
                    config.getString("database.url"),
                    config.getString("database.username"),
                    config.getString("database.password")
            );
        }
    }
