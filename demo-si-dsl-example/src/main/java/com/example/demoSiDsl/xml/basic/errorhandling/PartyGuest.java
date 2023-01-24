package com.example.demoSiDsl.xml.basic.errorhandling;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@MessageEndpoint
@Profile("ErrorhandlingConfig")
public class PartyGuest {

    
    public void onInvitation(Invitation invitation) {
        log.info("Guest is throwing exception");
        throw new RuntimeException("Wrong address");

    }


}