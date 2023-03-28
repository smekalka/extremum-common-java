package io.extremum.exampleproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoReactiveDataAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = {"io.extremum.exampleproject"},
        exclude = MongoReactiveDataAutoConfiguration.class)
public class ExampleProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleProjectApplication.class, args);
    }

}
