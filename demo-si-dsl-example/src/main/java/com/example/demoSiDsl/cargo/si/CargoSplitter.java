package com.example.demoSiDsl.cargo.si;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;
import org.springframework.messaging.Message;

import com.example.demoSiDsl.cargo.Cargo;

@MessageEndpoint
@Profile("cargo")
public class CargoSplitter {
    
    @Splitter(inputChannel = "cargoGWDefaultRequestChannel",
                outputChannel = "cargoSplitterOutputChannel"
        )
    public List<Cargo> splitCargoList(Message<List<Cargo>> message) {
        return message.getPayload();
    }
}
