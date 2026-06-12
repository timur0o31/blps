package org.example.blps.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@EntityScan(basePackages = "org.example.blps.entity")
@EnableJpaRepositories(basePackages = {"org.example.blps.repository",
        "org.example.blps.worker"})
@SpringBootApplication(scanBasePackages = {"org.example.blps.worker", "org.example.blps.mapper"})
public class WorkerApplication extends SpringBootServletInitializer {
    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(WorkerApplication.class);
    }
}
