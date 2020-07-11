package guru.springframework.sbmbeerorderservice.statemachine;

import guru.springframework.sbmbeerorderservice.domain.BeerOrder;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import guru.springframework.sbmbeerorderservice.repositories.BeerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@RequiredArgsConstructor
@Component
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatusEnum, BeerOrderEventEnum> {

    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void postStateChange(State<BeerOrderStatusEnum, BeerOrderEventEnum> state, Message<BeerOrderEventEnum> message,
                                Transition<BeerOrderStatusEnum, BeerOrderEventEnum> transition, StateMachine<BeerOrderStatusEnum, BeerOrderEventEnum> stateMachine) {
        Optional.ofNullable(message).ifPresent(m -> {
            Optional.ofNullable((String) m.getHeaders().get(BEER_ORDER_HEADER_ID)).ifPresent(beerOrderId -> {
                BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(beerOrderId));
                beerOrder.setOrderStatus(state.getId());

                beerOrderRepository.saveAndFlush(beerOrder);
            });
        });
    }
}
