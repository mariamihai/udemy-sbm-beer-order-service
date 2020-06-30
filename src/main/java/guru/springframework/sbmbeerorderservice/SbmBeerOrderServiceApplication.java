package guru.springframework.sbmbeerorderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;

@SpringBootApplication(exclude = ArtemisAutoConfiguration.class)
public class SbmBeerOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SbmBeerOrderServiceApplication.class, args);
    }

}
