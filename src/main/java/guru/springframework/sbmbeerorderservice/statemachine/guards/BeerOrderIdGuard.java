package guru.springframework.sbmbeerorderservice.statemachine.guards;

import guru.springframework.sbmbeerorderservice.domain.BeerOrderEventEnum;
import guru.springframework.sbmbeerorderservice.domain.BeerOrderStatusEnum;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.guard.Guard;
import org.springframework.stereotype.Component;

import static guru.springframework.sbmbeerorderservice.statemachine.BeerOrderStateMachineConfig.BEER_ORDER_HEADER_ID;

@Component
public class BeerOrderIdGuard implements Guard<BeerOrderStatusEnum, BeerOrderEventEnum> {

    @Override
    public boolean evaluate(StateContext<BeerOrderStatusEnum, BeerOrderEventEnum> context) {
        return context.getMessageHeader(BEER_ORDER_HEADER_ID) != null;
    }
}
