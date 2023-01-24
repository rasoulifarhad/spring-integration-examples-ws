package com.example.demoSiDsl.cargo.si;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.annotation.MessageEndpoint;

import com.example.demoSiDsl.cargo.Cargo;

@Profile("cargo")
@MessageEndpoint
public class CargoFilter {
    private static final long CARGO_WEIGHT_LIMIT = 1_000;

    @Filter(inputChannel = "cargoSplitterOutputChannel" ,
                outputChannel = "cargoFilterOutputChannel" ,
                discardChannel = "cargoFilterDiscardChannel"        
        )
    public boolean filterIfCargoWeightExceedsLimit(Cargo cargo) {
        return cargo.getWeight() <= CARGO_WEIGHT_LIMIT;
    }

}
