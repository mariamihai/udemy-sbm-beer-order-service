package guru.springframework.sbmbeerorderservice.services;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.sbmbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Override
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @Override
    public void validateBeerOrder(UUID beerOrderId, boolean valid) {
        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderId);

        if(valid) {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

            allocateValidBeerOrder(beerOrderId);
        } else {
            sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILURE);
        }
    }

    @Override
    public void allocateValidBeerOrder(UUID beerOrderId) {
        BeerOrder beerOrder = beerOrderRepository.findOneById(beerOrderId);
        sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATE_ORDER);
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder,
                                    BeerOrderEventEnum event) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = build(beerOrder);

        Message<BeerOrderEventEnum> message = MessageBuilder.withPayload(event)
                .setHeader(BEER_ORDER_HEADER_ID, beerOrder.getId().toString())
                .build();

        stateMachine.sendEvent(message);
    }

    private StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine = factory.getStateMachine(beerOrder.getId());

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(stateMachineAccessor -> {
                    stateMachineAccessor.addStateMachineInterceptor(beerOrderStateChangeInterceptor);
                    stateMachineAccessor.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getOrderStatus(), null, null, null));
                });


        stateMachine.start();

        return stateMachine;
    }
}
