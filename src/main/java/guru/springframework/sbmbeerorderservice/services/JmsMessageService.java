package guru.springframework.sbmbeerorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import javax.jms.JMSException;
import javax.jms.TextMessage;

@Service
@RequiredArgsConstructor
public class JmsMessageService {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    public void sendJmsMessage(String destination, Object object, String typeForMessage) {
        jmsTemplate.send(destination, session -> {
            try {
                TextMessage message = session.createTextMessage(objectMapper.writeValueAsString(object));
                message.setStringProperty("_type", typeForMessage);

                return message;
            } catch (JsonProcessingException e) {
                throw new JMSException("Exception thrown");
            }
        });
    }
}
