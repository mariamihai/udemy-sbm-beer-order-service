package guru.springframework.sbmbeerorderservice.services;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void validateBeerOrder(UUID beerOrderId, boolean valid);
}
