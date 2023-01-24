package com.example.demoSiDsl.cargo.si;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;

import com.example.demoSiDsl.cargo.Cargo;

@Profile("cargo")
@MessagingGateway(name = "cargoGateway" , 
                    defaultRequestChannel = "cargoGWDefaultRequestChannel")
public interface CargoGateway {

    
    @Gateway
    void processCargoRequest(Message<List<Cargo>> message) ;
}
