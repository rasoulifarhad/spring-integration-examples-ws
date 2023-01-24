package com.example.demoSiDsl.cargo.si;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;

import com.example.demoSiDsl.cargo.Cargo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@MessageEndpoint
@Profile("cargo")
public class DiscardedCargoMessageListener {
 
    @ServiceActivator(inputChannel = "cargoFilterDiscardChannel")
    public void handleDiscardedCargo(Cargo cargo , @Header("CARGO_BATCH_ID") long batchId) {
        log.info("Message in Batch[" + batchId + "] is received with Discarded payload : " + cargo);
    }
}
