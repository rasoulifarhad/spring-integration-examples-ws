package com.example.demoSiDsl.cargo.si;

import org.springframework.context.annotation.Profile;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Router;

import com.example.demoSiDsl.cargo.Cargo;
import com.example.demoSiDsl.cargo.Cargo.ShippingType;

@MessageEndpoint
@Profile("cargo")
public class CargoRouter {
    
    @Router(inputChannel = "cargoFilterOutputChannel")
    public String route(Cargo cargo) {

        if(cargo.getShippingType() == ShippingType.DOMESTIC) {
            return "cargoRouterDomesticOutputChannel";
        } else if(cargo.getShippingType() == ShippingType.INTERNATIONAL) {
            return "cargoRouterInternationalOutputChannel";
        } 

        return "nullChannel"; 
    }
}
