package com.dappunq;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class DappApplication {
    public static void main(String[] args) {
        boolean rotateCommand = Arrays.stream(args)
                .anyMatch(arg -> arg.startsWith("--app.command=rotate-scraping-api-key"));

        if (rotateCommand) {
            System.setProperty("spring.main.web-application-type", "none");
            System.setProperty("spring.main.banner-mode", "off");
            System.setProperty("logging.level.root", "OFF");
        }

        SpringApplication application = new SpringApplication(DappApplication.class);
        if (rotateCommand) {
            application.setWebApplicationType(WebApplicationType.NONE);
        }
        application.run(args);
    }
}
