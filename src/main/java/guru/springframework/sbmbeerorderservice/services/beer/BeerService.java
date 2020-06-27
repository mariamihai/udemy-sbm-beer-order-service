package guru.springframework.sbmbeerorderservice.services.beer;

import guru.springframework.sbmbeerorderservice.services.beer.model.BeerDto;

import java.util.Optional;
import java.util.UUID;

public interface BeerService {

    Optional<BeerDto> getBeerById(UUID uuid);

    Optional<BeerDto> getBeerByUpc(String upc);
}
