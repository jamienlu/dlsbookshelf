package io.github.jamienlu.dlsbookshelf;

import io.github.jamienlu.dlsbookshelf.conf.RegistryConfigProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RegistryConfigProperties.class)
public class DlsbookshelfApplication {

    public static void main(String[] args) {
        SpringApplication.run(DlsbookshelfApplication.class, args);
    }

}
