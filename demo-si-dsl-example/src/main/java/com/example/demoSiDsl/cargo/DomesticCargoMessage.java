package com.example.demoSiDsl.cargo;

import lombok.Getter;


public class DomesticCargoMessage extends CargoMessage{

    @Getter
    private final Region region; 

    public DomesticCargoMessage(Cargo cargo, Region region) {
        super(cargo);
        this.region = region;
    }

}
