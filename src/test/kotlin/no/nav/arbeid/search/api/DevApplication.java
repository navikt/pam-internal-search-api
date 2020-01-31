package no.nav.arbeid.search.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
public class DevApplication {

    public static void main(String args []) {
        ArrayList<String> list = new ArrayList<>(Arrays.asList(args));
        list.add("--spring.config.location=classpath:/application-dev.yml");
        SpringApplication.run(DevApplication.class, list.toArray(new String[0]));
    }

}
