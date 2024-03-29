package com.example.demoSiDsl.cargo;

import java.util.Arrays;
public enum Region {

        NORTH(1), SOUTH(2), EAST(3), WEST(4);

        private int value;

        private Region(int value) {
            this.value = value;
        }

        public static Region fromValue(int value) {
            return Arrays.stream(Region.values())
                            .filter(region -> region.value == value)
                            .findFirst()
                            .get();
        }
    
}
