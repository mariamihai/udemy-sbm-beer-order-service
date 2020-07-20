package guru.springframework.sbmbeerorderservice.services;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.sbmbeerorderservice.repositories.BeerOrderRepository;
import guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateChangeInterceptor;
import guru.springframework.sbmbeerorderservice.web.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @Override
    @Transactional
    public void validateBeerOrder(UUID beerOrderId, boolean valid) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);

        optionalBeerOrder.ifPresentOrElse(beerOrder -> {
            if(valid) {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

                allocateValidBeerOrder(beerOrderId);
            } else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILURE);
            }
        }, () -> log.error(" 1 Order not found, beerOrderId: " + beerOrderId));
    }

    @Override
    public void allocateValidBeerOrder(UUID beerOrderId) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATE_ORDER),
                                 () -> log.error(" 2 Order not found, beerOrderId: " + beerOrderId));
    }

    @Override
    @Transactional
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> {
                    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);
                    updateAllocatedQty(beerOrderDto, beerOrder);
                }, () -> log.error(" 3 Order not found, beerOrderId: " + beerOrderDto.getId()));
    }

    @Override
    @Transactional
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED),
                                () -> log.error(" 4 Order not found, beerOrderId: " + beerOrderDto.getId()));
    }

    @Override
    @Transactional
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> {
                    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);
                    updateAllocatedQty(beerOrderDto, beerOrder);
                }, () -> log.error(" 5 Order not found, beerOrderId: " + beerOrderDto.getId()));
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(allocatedOrder -> {
                    allocatedOrder.getBeerOrderLines()
                            .forEach(line -> beerOrderDto.getBeerOrderLines()
                                    .stream()
                                    .filter(lineDto -> lineDto.getId().equals(line.getId())).findFirst()
                                    .ifPresent(lineDto -> line.setQuantityAllocated(lineDto.getQuantityAllocated())));

                    beerOrderRepository.saveAndFlush(beerOrder);
                }, () -> log.error(" 6 Order not found, beerOrderId: " + beerOrderDto.getId()));
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
