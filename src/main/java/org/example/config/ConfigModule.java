package org.example.config;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;


public class ConfigModule extends AbstractModule {
    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public Config provideConfig() {
        return ConfigFactory.load(); // This loads application.conf by default
    }
}