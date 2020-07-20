package guru.springframework.sbmbeerorderservice.services.testcomponents;

import guru.springframework.sbmbeerorderservice.config.JmsConfig;
import guru.springframework.sbmbeerorderservice.services.JmsMessageService;
import guru.springframework.sbmbeerorderservice.web.model.events.ValidateBeerOrderRequest;
import guru.springframework.sbmbeerorderservice.web.model.events.ValidateBeerOrderResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestBeerOrderValidationListener {

    private final JmsMessageService jmsMessageService;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_QUEUE)
    public void listen(Message<ValidateBeerOrderRequest> message) {
        ValidateBeerOrderRequest request = message.getPayload();

        ValidateBeerOrderResult result = ValidateBeerOrderResult.builder()
                .beerOrderId(request.getBeerOrderDto().getId())
                .valid(true)
                .build();
        jmsMessageService.sendJmsMessage(JmsConfig.VALIDATE_ORDER_RESULT_QUEUE, result, ValidateBeerOrderResult.class.getSimpleName()) ;
    }
}
