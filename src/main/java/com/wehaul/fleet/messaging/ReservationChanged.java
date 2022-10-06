package com.wehaul.fleet.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationChanged {
    private int truckId;
    private String eventType;
    private int currentMiles;
}
