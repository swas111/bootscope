package io.github.swas111.examples.demostarter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * A bare Spring Boot app. It declares no tools itself — {@code bootscope_ping} appears only
 * because the Bootscope starter is on the classpath and auto-configures it.
 */
@SpringBootApplication
public class DemoStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoStarterApplication.class, args);
    }
}
