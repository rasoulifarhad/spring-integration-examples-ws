package com.example.demoSiDsl.xml.basic.errorhandling;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@MessageEndpoint
@Profile("ErrorhandlingConfig")
public class PartyHost {
    
    private final AtomicInteger counter = new AtomicInteger(0);


    public Invitation  nextInvitation() {
        return new Invitation(counter.incrementAndGet());
    }

    
    public void onInvitationFailed(Invitation invitation) {

        log.info("Host received failure notification for: " + invitation);

    }
}