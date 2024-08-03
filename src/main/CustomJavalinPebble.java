package org.example;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import io.javalin.rendering.template.JavalinPebble;

public class CustomJavalinPebble extends JavalinPebble {
    public CustomJavalinPebble() {
        super(new PebbleEngine.Builder()
                .loader(new ClasspathLoader())
                .build());
    }
}