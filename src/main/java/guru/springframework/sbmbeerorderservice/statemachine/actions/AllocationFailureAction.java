package guru.springframework.sbmbeerorderservice.statemachine.actions;

import guru.springframework.sbmbeerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.sbmbeerorderservice.services.JmsMessageService;
import guru.springframework.sbmbeerorderservice.web.model.events.AllocationFailureEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static guru.springframework.sbmbeerorderservice.config.JmsConfig.ALLOCATE_ORDER_FAILED_QUEUE;
import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocationFailureAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final JmsMessageService jmsMessageService;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        UUID beerOrderId = UUID.fromString((String) context.getMessageHeader(BEER_ORDER_HEADER_ID));

        doCompensatingTransaction(beerOrderId);
    }

    private void doCompensatingTransaction(UUID beerOrderId) {
        log.error("Allocation failed for beerOrderId: " + beerOrderId);

        jmsMessageService.sendJmsMessage(ALLOCATE_ORDER_FAILED_QUEUE,
                                         AllocationFailureEvent.builder().beerOrderId(beerOrderId).build(),
                                         AllocationFailureEvent.class.getSimpleName());
    }
}
