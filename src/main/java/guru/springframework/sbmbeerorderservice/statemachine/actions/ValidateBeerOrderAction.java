package guru.springframework.sbmbeerorderservice.statemachine.actions;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.sbmbeerorderservice.web.model.events.ValidateBeerOrderRequest;
import guru.springframework.sbmbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.sbmbeerorderservice.web.mappers.BeerOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static guru.springframework.sbmbeerorderservice.config.JmsConfig.VALIDATE_ORDER_QUEUE;
import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateBeerOrderAction implements Action<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;
    private final JmsTemplate jmsTemplate;
    private final BeerOrderMapper beerOrderMapper;

    @Override
    public void execute(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        UUID beerOrderId = UUID.fromString((String) context.getMessageHeader(BEER_ORDER_HEADER_ID));
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);

        jmsTemplate.convertAndSend(VALIDATE_ORDER_QUEUE, new ValidateBeerOrderRequest(beerOrderMapper.beerOrderToDto(beerOrder)));

        log.debug("Sent validation request for beerOrderId - " + beerOrderId);
    }
}
