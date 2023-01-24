package com.example.demoSiDsl.xml.basic.errorhandling;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Component;


@Component
@Profile("ErrorhandlingConfig")
@MessageEndpoint
public class ErrorUnwrapper {

    @Transformer
    public Message<?> transform(ErrorMessage errorMessage) {

        return ((MessagingException)errorMessage.getPayload()).getFailedMessage();

    }

}