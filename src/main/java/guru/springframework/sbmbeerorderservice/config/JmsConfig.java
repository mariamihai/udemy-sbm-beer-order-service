package guru.springframework.sbmbeerorderservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.sbmbeerorderservice.web.model.events.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmsConfig {

    public static final String VALIDATE_ORDER_QUEUE = "validate-order";
    public static final String VALIDATE_ORDER_RESULT_QUEUE = "validate-order-result";
    public static final String ALLOCATE_ORDER_QUEUE = "allocate-order";
    public static final String ALLOCATE_ORDER_RESPONSE_QUEUE = "allocate-order-response";
    public static final String ALLOCATE_ORDER_FAILED_QUEUE = "allocate-order-failed";

    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        converter.setTypeIdMappings(setClassMappings());

        converter.setObjectMapper(objectMapper);

        return converter;
    }

    private Map<String,Class<?>> setClassMappings() {
        Map<String,Class<?>> typeIdMappings = new HashMap<>();
        typeIdMappings.put(ValidateBeerOrderRequest.class.getSimpleName(), ValidateBeerOrderRequest.class);
        typeIdMappings.put(ValidateBeerOrderResult.class.getSimpleName(), ValidateBeerOrderResult.class);
        typeIdMappings.put(AllocateBeerOrderRequest.class.getSimpleName(), AllocateBeerOrderRequest.class);
        typeIdMappings.put(AllocateBeerOrderResult.class.getSimpleName(), AllocateBeerOrderResult.class);
        typeIdMappings.put(AllocationFailureEvent.class.getSimpleName(), AllocationFailureEvent.class);

        return typeIdMappings;
    }
}
