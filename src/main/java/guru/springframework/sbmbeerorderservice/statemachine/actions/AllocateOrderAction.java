package guru.springframework.sbmbeerorderservice.statemachine.actions;

import guru.springframework.sbmbeerorderservice.config.JmsConfig;
import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.sbmbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.sbmbeerorderservice.services.JmsMessageService;
import guru.springframework.sbmbeerorderservice.web.mappers.BeerOrderMapper;
import guru.springframework.sbmbeerorderservice.web.model.events.AllocateBeerOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllocateOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsMessageService jmsMessageService;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        UUID beerOrderId = UUID.fromString((String) context.getMessageHeader(BEER_ORDER_HEADER_ID));
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);

        optionalBeerOrder.ifPresentOrElse(beerOrder -> {
            jmsMessageService.sendJmsMessage(JmsConfig.ALLOCATE_ORDER_QUEUE,
                    AllocateBeerOrderRequest.builder().beerOrderDto(beerOrderMapper.beerOrderToDto(beerOrder)).build(),
                    AllocateBeerOrderRequest.class.getSimpleName());

            log.debug("Sent Allocation Request for beerOrderId - " + beerOrderId);
        }, () -> log.error("Couldn't send Allocation Request for beerOrderId - " + beerOrderId));
    }
}
