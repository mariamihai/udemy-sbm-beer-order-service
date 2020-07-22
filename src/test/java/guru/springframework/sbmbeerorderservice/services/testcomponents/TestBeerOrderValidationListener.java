package guru.springframework.sbmbeerorderservice.services.testcomponents;

import guru.springframework.sbmbeerorderservice.config.JmsConfig;
import guru.springframework.sbmbeerorderservice.services.JmsMessageService;
import guru.springframework.sbmbeerorderservice.web.model.events.ValidateBeerOrderRequest;
import guru.springframework.sbmbeerorderservice.web.model.events.ValidateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import static guru.springframework.sbmbeerorderservice.services.testcomponents.ITUtil.VALIDATION_CANCEL_ORDER;
import static guru.springframework.sbmbeerorderservice.services.testcomponents.ITUtil.FAILED_VALIDATION;

@Component
@RequiredArgsConstructor
public class TestBeerOrderValidationListener {

    private final JmsMessageService jmsMessageService;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message<ValidateBeerOrderRequest> message) {
        ValidateBeerOrderRequest request = message.getPayload();

        if(isIgnoredMessageToCancelOrder(request.getBeerOrderDto().getCustomerRef())) return;

        ValidateBeerOrderResult result = ValidateBeerOrderResult.builder()
                .beerOrderId(request.getBeerOrderDto().getId())
                .valid(isValidRequest(request.getBeerOrderDto().getCustomerRef()))
                .build();
        jmsMessageService.sendJmsMessage(JmsConfig.VALIDATE_ORDER_RESULT_QUEUE, result, ValidateBeerOrderResult.class.getSimpleName()) ;
    }

    private boolean isValidRequest(String customerRef) {
        return !FAILED_VALIDATION.equals(customerRef);
    }

    private boolean isIgnoredMessageToCancelOrder(String customerRef) {
        return VALIDATION_CANCEL_ORDER.equals(customerRef);
    }
}
