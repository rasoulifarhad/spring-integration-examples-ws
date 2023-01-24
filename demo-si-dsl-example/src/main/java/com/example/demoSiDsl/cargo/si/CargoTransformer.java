package com.example.demoSiDsl.cargo.si;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Transformer;

import com.example.demoSiDsl.cargo.Cargo;
import com.example.demoSiDsl.cargo.DeliveryOption;
import com.example.demoSiDsl.cargo.DomesticCargoMessage;
import com.example.demoSiDsl.cargo.InternationalCargoMessage;
import com.example.demoSiDsl.cargo.Region;

@MessageEndpoint
@Profile("cargo")
public class CargoTransformer {
    
    @Transformer(inputChannel = "cargoRouterDomesticOutputChannel",
                    outputChannel = "cargoTransformerOutputChannel"
            )
    public DomesticCargoMessage transformDomesticCargo(Cargo cargo) {
        return new DomesticCargoMessage(cargo, Region.fromValue(cargo.getRegion()));
    }

    @Transformer(inputChannel = "cargoRouterInternationalOutputChannel",
                    outputChannel = "cargoTransformerOutputChannel"
            )
    public InternationalCargoMessage transformInternationalCargo(Cargo cargo ) {
        return new InternationalCargoMessage(cargo, getDeliveryOption(cargo.getDeliveryDayCommitment()));

    }

    private DeliveryOption getDeliveryOption(int deliveryDayCommitment) {
        if (deliveryDayCommitment == 1) {
            return DeliveryOption.NEXT_FLIGHT;
        } else if (deliveryDayCommitment == 2) {
            return DeliveryOption.PRIORITY;
        } else if (deliveryDayCommitment > 2 && deliveryDayCommitment < 5) {
            return DeliveryOption.ECONOMY;
        } else {
            return DeliveryOption.STANDART;
        }
    }
}
