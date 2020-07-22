package guru.springframework.sbmbeerorderservice.services;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;

import java.util.UUID;

public interface BeerOrderManager {

    BeerOrder newBeerOrder(BeerOrder beerOrder);

    void validateBeerOrder(UUID beerOrderId, boolean valid);

    void allocateValidBeerOrder(UUID beerOrderId);

    void beerOrderAllocationPassed(BeerOrderDto beerOrderDto);

    void beerOrderAllocationFailed(BeerOrderDto beerOrderDto);

    void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto);

    void beerOrderPickedUp(UUID beerOrderId);

    void cancelBeerOrder(UUID beerOrderId);
}
