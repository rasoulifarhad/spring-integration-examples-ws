package com.example.demoSiDsl.cargo;

public class InternationalCargoMessage extends CargoMessage{
    private final DeliveryOption deliveryOption;
    public InternationalCargoMessage(Cargo cargo, DeliveryOption deliveryOption) {
        super(cargo);
        this.deliveryOption = deliveryOption;
    }

    public DeliveryOption getDeliveryOption() {
        return deliveryOption;
    }

    @Override
    public String toString() {
        return "InternationalCargoMessage [cargo=" + super.toString() + ", deliveryOption=" + deliveryOption + "]";
    }
}
