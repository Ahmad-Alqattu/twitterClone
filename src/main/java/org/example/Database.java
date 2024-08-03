package org.example;

import com.google.inject.Inject;
import com.typesafe.config.Config;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;

public class Database {
    private final Jdbi jdbi;

    @Inject
    public Database(Config config) {
        this.jdbi = Jdbi.create(
                config.getString("db.url"),
                config.getString("db.user"),
                config.getString("db.password")
        );
        this.jdbi.setSqlLogger(new Slf4JSqlLogger());
    }




    public Jdbi getJdbi() {
        return jdbi;
    }
}
