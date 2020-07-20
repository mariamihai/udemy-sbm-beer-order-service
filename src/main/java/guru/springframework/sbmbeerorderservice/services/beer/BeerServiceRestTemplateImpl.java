package guru.springframework.sbmbeerorderservice.services.beer;

import guru.springframework.sbmbeerorderservice.web.model.events.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@ConfigurationProperties(prefix = "sbm.brewery", ignoreUnknownFields = false)
@Component
public class BeerServiceRestTemplateImpl implements BeerService {

    private String beerServiceHost;
    private String beerPath;
    private String beerUpcPath;

    private final RestTemplate restTemplate;

    public BeerServiceRestTemplateImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setBeerServiceHost(String host) {
        beerServiceHost = host;
    }

    public void setBeerPath(String path) {
        beerPath = path;
    }

    public void setBeerUpcPath(String upcPath) {
        beerUpcPath = upcPath;
    }

    public String getBeerServiceHost() {
        return beerServiceHost;
    }

    public String getBeerPath() {
        return beerPath;
    }

    public String getBeerUpcPath() {
        return beerUpcPath;
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID uuid) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + beerPath + uuid.toString(), BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String upc) {
        return Optional.of(restTemplate.getForObject(beerServiceHost + beerUpcPath + upc, BeerDto.class));
    }
}
