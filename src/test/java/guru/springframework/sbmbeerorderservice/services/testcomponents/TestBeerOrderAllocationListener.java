package guru.springframework.sbmbeerorderservice.services.testcomponents;

import guru.springframework.sbmbeerorderservice.config.JmsConfig;
import guru.springframework.sbmbeerorderservice.services.JmsMessageService;
import guru.springframework.sbmbeerorderservice.web.model.events.AllocateBeerOrderRequest;
import guru.springframework.sbmbeerorderservice.web.model.events.AllocateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestBeerOrderAllocationListener {

    private final JmsMessageService jmsMessageService;

    @JmsListener(destination = JmsConfig.ALLOCATE_ORDER_QUEUE)
    public void listen(AllocateBeerOrderRequest request) {
        request.getBeerOrderDto().getBeerOrderLines().forEach(line -> line.setQuantityAllocated(line.getOrderQuantity()));

        AllocateBeerOrderResult result = AllocateBeerOrderResult.builder()
                .allocationError(false)
                .pendingInventory(false)
                .beerOrderDto(request.getBeerOrderDto()).build();

        jmsMessageService.sendJmsMessage(JmsConfig.ALLOCATE_ORDER_RESPONSE_QUEUE,
                                         result,
                                         AllocateBeerOrderResult.class.getSimpleName()) ;
    }
}
