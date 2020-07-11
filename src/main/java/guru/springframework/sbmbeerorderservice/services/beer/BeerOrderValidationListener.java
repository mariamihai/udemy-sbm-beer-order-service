package guru.springframework.sbmbeerorderservice.services.beer;

import guru.springframework.sbmbeerorderservice.config.JmsConfig;
import guru.springframework.sbmbeerorderservice.services.BeerOrderManager;
import guru.springframework.sbmbeerorderservice.web.model.events.ValidateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeerOrderValidationListener {

    private final BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESULT_QUEUE)
    public void listen(ValidateBeerOrderResult result) {
        log.debug("Validation result for beerOrderId - " + result.getBeerOrderId() + " is " + result.isValid());

        beerOrderManager.validateBeerOrder(result.getBeerOrderId(), result.isValid());
    }
}
