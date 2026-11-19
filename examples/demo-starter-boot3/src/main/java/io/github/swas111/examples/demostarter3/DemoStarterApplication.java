package io.github.swas111.examples.demostarter3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Bare Spring Boot 3.5 app. {@code bootscope_ping} appears only via the Bootscope starter.
 */
@SpringBootApplication
public class DemoStarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoStarterApplication.class, args);
    }
}
