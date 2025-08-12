package com.learnd.learnd_main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
                // your app package
        "com.learnd.integration"  // integration service package
})
public class LearndMainApplication {

    public static void main(String[] args) {

        SpringApplication.run(LearndMainApplication.class, args);
        System.out.println("Hello World!");

    }

}
