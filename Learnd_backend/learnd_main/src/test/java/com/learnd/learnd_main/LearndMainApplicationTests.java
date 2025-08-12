package com.learnd.learnd_main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.learnd.learnd_main",        // your app package
        "com.learnd.integration"  // integration service package
})
@SpringBootTest
class LearndMainApplicationTests {

    @Test
    void contextLoads() {
    }

}
