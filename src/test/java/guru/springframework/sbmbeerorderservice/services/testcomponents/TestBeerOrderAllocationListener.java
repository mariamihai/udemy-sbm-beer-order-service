package guru.springframework.sbmbeerorderservice.services.testcomponents;

import guru.springframework.sbmbeerorderservice.config.JmsConfig;
import guru.springframework.sbmbeerorderservice.services.JmsMessageService;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import guru.springframework.sbmbeerorderservice.web.model.events.AllocateBeerOrderRequest;
import guru.springframework.sbmbeerorderservice.web.model.events.AllocateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import static guru.springframework.sbmbeerorderservice.services.testcomponents.ITUtil.*;

@Component
@RequiredArgsConstructor
public class TestBeerOrderAllocationListener {

    private final JmsMessageService jmsMessageService;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateBeerOrderRequest request) {
        if(isIgnoredMessageToCancelOrder(request.getBeerOrderDto().getCustomerRef())) return;

        boolean pendingInventory = isPendingInventory(request.getBeerOrderDto().getCustomerRef());
        allocateBeerOrderLines(request.getBeerOrderDto(), pendingInventory);

        AllocateBeerOrderResult result = AllocateBeerOrderResult.builder()
                .allocationError(isAllocationError(request.getBeerOrderDto().getCustomerRef()))
                .pendingInventory(pendingInventory)
                .beerOrderDto(request.getBeerOrderDto()).build();

        jmsMessageService.sendJmsMessage(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                                         result,
                                         AllocateBeerOrderResult.class.getSimpleName()) ;
    }

    private void allocateBeerOrderLines(BeerOrderDto beerOrderDto, boolean pendingInventory) {
        if(pendingInventory) {
            beerOrderDto.getBeerOrderLines()
                    .forEach(line -> line.setQuantityAllocated(line.getOrderQuantity() - MINUS_BEERS_FOR_PARTIAL_ALLOCATION));
        } else {
            beerOrderDto.getBeerOrderLines()
                    .forEach(line -> line.setQuantityAllocated(line.getOrderQuantity()));
        }
    }

    private boolean isAllocationError(String customerRef) {
        return FAILED_ALLOCATION.equals(customerRef);
    }

    private boolean isPendingInventory(String customerRef) {
        return PARTIAL_ALLOCATION.equals(customerRef);
    }

    private boolean isIgnoredMessageToCancelOrder(String customerRef) {
        return ALLOCATION_CANCEL_ORDER.equals(customerRef);
    }
}
