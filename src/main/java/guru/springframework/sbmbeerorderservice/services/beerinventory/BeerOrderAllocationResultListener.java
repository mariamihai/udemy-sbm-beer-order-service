package guru.springframework.sbmbeerorderservice.services.beerinventory;

import guru.springframework.sbmbeerorderservice.config.JmsConfig;
import guru.springframework.sbmbeerorderservice.services.BeerOrderManager;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import guru.springframework.sbmbeerorderservice.web.model.events.AllocateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderAllocationResultListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE)
    public void listen(AllocateBeerOrderResult result) {
        BeerOrderDto beerOrderDto = result.getBeerOrderDto();
        log.debug("Allocation result received for " + beerOrderDto);


        if(result.isAllocationError()) {
            beerOrderManager.beerOrderAllocationFailed(beerOrderDto);
        } else if(result.isPendingInventory()) {
            beerOrderManager.beerOrderAllocationPendingInventory(beerOrderDto);
        } else {
            beerOrderManager.beerOrderAllocationPassed(beerOrderDto);
        }
    }
}
