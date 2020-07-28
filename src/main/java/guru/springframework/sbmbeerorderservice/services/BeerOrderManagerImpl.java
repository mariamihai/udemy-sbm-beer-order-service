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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BeerOrderManagerImpl implements BeerOrderManager {

    private final BeerOrderRepository beerOrderRepository;
    private final StateMachineFactory<BeerOrderStatusEnum, BeerOrderEventEnum> factory;
    private final BeerOrderStateChangeInterceptor beerOrderStateChangeInterceptor;

    private static final String ERROR_MESSAGE = " Order not found, beerOrderId: ";

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setOrderStatus(BeerOrderStatusEnum.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);
        log.debug("Saved Beer Order: " + savedBeerOrder.getId());

        sendBeerOrderEvent(savedBeerOrder, BeerOrderEventEnum.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @Override
    public void validateBeerOrder(UUID beerOrderId, boolean valid) {
        awaitBeforeValidation(beerOrderId);
        validate(beerOrderId, valid);
    }

    @Transactional
    public void validate(UUID beerOrderId, boolean valid) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);

        optionalBeerOrder.ifPresentOrElse(beerOrder -> {
            if(valid) {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_PASSED);

                awaitForStatus(beerOrderId, BeerOrderStatusEnum.VALIDATED);
                allocateValidBeerOrder(beerOrderId);
            } else {
                sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.VALIDATION_FAILURE);
            }
        }, () -> log.error(ERROR_MESSAGE + beerOrderId));
    }

    @Override
    public void allocateValidBeerOrder(UUID beerOrderId) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATE_ORDER),
                                 () -> log.error(ERROR_MESSAGE + beerOrderId));
    }

    @Override
    @Transactional
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> {
                    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_SUCCESS);

                    awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.ALLOCATED);
                    updateAllocatedQty(beerOrderDto);
                }, () -> log.error(ERROR_MESSAGE + beerOrderDto.getId()));
    }

    @Override
    @Transactional
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_FAILED),
                                () -> log.error(ERROR_MESSAGE + beerOrderDto.getId()));
    }

    @Override
    @Transactional
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(beerOrder -> {
                    sendBeerOrderEvent(beerOrder, BeerOrderEventEnum.ALLOCATION_NO_INVENTORY);

                    awaitForStatus(beerOrder.getId(), BeerOrderStatusEnum.PENDING_INVENTORY);
                    updateAllocatedQty(beerOrderDto);
                }, () -> log.error(ERROR_MESSAGE + beerOrderDto.getId()));
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderDto.getId());

        optionalBeerOrder
                .ifPresentOrElse(allocatedOrder -> {
                    allocatedOrder.getBeerOrderLines()
                            .forEach(line -> beerOrderDto.getBeerOrderLines()
                                    .stream()
                                    .filter(lineDto -> lineDto.getId().equals(line.getId())).findFirst()
                                    .ifPresent(lineDto -> line.setQuantityAllocated(lineDto.getQuantityAllocated())));

                    beerOrderRepository.saveAndFlush(allocatedOrder);
                }, () -> log.error(ERROR_MESSAGE + beerOrderDto.getId()));
    }

    @Override
    public void beerOrderPickedUp(UUID beerOrderId) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);
        optionalBeerOrder
                .ifPresentOrElse(pickedUpOrder -> sendBeerOrderEvent(pickedUpOrder, BeerOrderEventEnum.BEER_ORDER_PICKED_UP),
                                 () -> log.error(ERROR_MESSAGE + beerOrderId));
    }

    @Override
    public void cancelBeerOrder(UUID beerOrderId) {
        Optional<BeerOrder> optionalBeerOrder = beerOrderRepository.findById(beerOrderId);
        optionalBeerOrder
                .ifPresentOrElse(canceledBeerOrder -> sendBeerOrderEvent(canceledBeerOrder, BeerOrderEventEnum.CANCEL_ORDER),
                        () -> log.error(ERROR_MESSAGE + beerOrderId));
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

    private void awaitForStatus(UUID beerOrderId, BeerOrderStatusEnum statusEnum) {
        log.debug("Waiting for the status change for " + beerOrderId + " to status " + statusEnum.name());

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
            }

            beerOrderRepository.findById(beerOrderId).ifPresentOrElse(beerOrder -> {
                if (beerOrder.getOrderStatus().equals(statusEnum)) {
                    found.set(true);
                }
            }, () -> log.debug(" beerOrderId not found - " + beerOrderId));

            if (!found.get()) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Error encountered");
                }
            }
        }
    }

    private void awaitBeforeValidation(UUID beerOrderId) {
        log.debug("Waiting for the db save before validation for " + beerOrderId);

        AtomicBoolean found = new AtomicBoolean(false);
        AtomicInteger loopCount = new AtomicInteger(0);

        while (!found.get()) {
            if (loopCount.incrementAndGet() > 10) {
                found.set(true);
            }

            beerOrderRepository.findById(beerOrderId).ifPresent(beerOrder -> found.set(true));

            if (!found.get()) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Error encountered");
                }
            }
        }
    }
}