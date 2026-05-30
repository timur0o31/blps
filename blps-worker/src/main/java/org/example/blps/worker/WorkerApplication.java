package org.example.blps.worker;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.WebApplicationType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@EntityScan(basePackages = "org.example.blps.entity")
@EnableJpaRepositories(basePackages = {
        "org.example.blps.repository",
        "org.example.blps.worker"
})
@SpringBootApplication(scanBasePackages = {
        "org.example.blps.worker",
        "org.example.blps.mapper"})
public class WorkerApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(WorkerApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }
}
