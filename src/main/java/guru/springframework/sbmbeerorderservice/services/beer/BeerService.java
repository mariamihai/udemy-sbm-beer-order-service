package guru.springframework.sbmbeerorderservice.services.beer;

import guru.springframework.sbmbeerorderservice.web.model.events.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<BeerDto> getBeerById(UUID uuid);

    Optional<BeerDto> getBeerByUpc(String upc);

    String getBeerServiceHost();

    String getBeerPath();

    String getBeerUpcPath();
}
