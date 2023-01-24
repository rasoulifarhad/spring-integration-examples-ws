package com.example.demoSiDsl.cargo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import com.example.demoSiDsl.cargo.Cargo.ShippingType;
import com.example.demoSiDsl.cargo.si.CargoGateway;

@Component
@Profile("cargo")
public class CargoApp {
   
    @Bean
//    @DependsOn("client")
    ApplicationRunner runner(CargoGateway gateway) {
        return args -> {
            getCargoBatchMap().forEach(
                (batchId, cargoList) -> gateway.processCargoRequest(MessageBuilder
                                                                            .withPayload(cargoList)
                                                                            .setHeader("CARGO_BATCH_ID", batchId)
                                                                            .build()));        };
    }

/**
     * Creates a sample cargo batch map covering multiple batches and returns.
     *
     * @return cargo batch map
     */
    private static Map<Integer, List<Cargo>> getCargoBatchMap() {
        Map<Integer, List<Cargo>> cargoBatchMap = new HashMap<>();
        cargoBatchMap.put(1, Arrays.asList(

                new Cargo.CargoBuilder(1, "Receiver_Name1", "Address1", 0.5, ShippingType.DOMESTIC)
                            .setRegion(1).setDescription("Radio").build(),
                //Second cargo is filtered due to weight limit          
                new Cargo.CargoBuilder(2, "Receiver_Name2", "Address2", 2_000, ShippingType.INTERNATIONAL)
                            .setDeliveryDayCommitment(3).setDescription("Furniture").build(),
                new Cargo.CargoBuilder(3, "Receiver_Name3", "Address3", 5, ShippingType.INTERNATIONAL)
                            .setDeliveryDayCommitment(2).setDescription("TV").build(),
                //Fourth cargo is not processed due to no shipping type found           
                new Cargo.CargoBuilder(4, "Receiver_Name4", "Address4", 8, null)
                            .setDeliveryDayCommitment(2).setDescription("Chair").build()));

        cargoBatchMap.put(2, Arrays.asList(
                //Fifth cargo is filtered due to weight limit
                new Cargo.CargoBuilder(5, "Receiver_Name5", "Address5", 1_200, ShippingType.DOMESTIC)
                            .setRegion(2).setDescription("Refrigerator").build(),
                new Cargo.CargoBuilder(6, "Receiver_Name6", "Address6", 20, ShippingType.DOMESTIC)
                            .setRegion(3).setDescription("Table").build(),
                //Seventh cargo is not processed due to no shipping type found
                new Cargo.CargoBuilder(7, "Receiver_Name7", "Address7", 5, null)
                            .setDeliveryDayCommitment(1).setDescription("TV").build()));

        cargoBatchMap.put(3, Arrays.asList(
                new Cargo.CargoBuilder(8, "Receiver_Name8", "Address8", 200, ShippingType.DOMESTIC)
                            .setRegion(2).setDescription("Washing Machine").build(),
                new Cargo.CargoBuilder(9, "Receiver_Name9", "Address9", 4.75, ShippingType.INTERNATIONAL)
                            .setDeliveryDayCommitment(1).setDescription("Document").build()));

        return Collections.unmodifiableMap(cargoBatchMap);
    }    
}
