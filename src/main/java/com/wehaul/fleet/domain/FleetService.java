package com.wehaul.fleet.domain;

import com.wehaul.fleet.data.TruckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FleetService {

    private final TruckRepository truckRepo;
    private final DomainEventPublisher domainEventPublisher;

    public Truck addTruck(NewTruck newTruck) {

        if (newTruck.miles() <  0) {
            throw new RuntimeException("miles must 0 or greater");
        }

        Truck truck = Truck.builder()
                .miles(newTruck.miles())
                .lastInspectionMiles(0)
                .available(true)
                .availabilityReason("new truck")
                .build();

        // save to database
        Truck savedTruck = truckRepo.create(truck);
        log.info(" >>> truck saved to database: [{}]", savedTruck);

        publishAvailability(savedTruck.getTruckId(), true);

        return savedTruck;
    }

    public List<Truck> getTrucks() {
        return truckRepo.findAll();
    }

    public void sendTruckForInspection(int truckId) {
        Truck truck = claimTruck(truckId, "sent for inspection");

        truck.setLastInspectionMiles(truck.getMiles());
        truckRepo.update(truck);
    }

    // todo: what if new miles are less?

    public void returnTruckFromInspection(int truckId) {
        int currentMiles = getTruckMiles(truckId);
        releaseTruck(truckId, "returned from inspection", currentMiles);
    }

    private int getTruckMiles(int truckId) {
        return truckRepo.findById(truckId)
                .map(Truck::getMiles)
                .orElseThrow(() -> new RuntimeException("truck not found"));
    }

    public Truck claimTruck(int truckId, String claimDetails) {

        // throw if truck not available
        Optional<Truck> truckOptional = truckRepo.findById(truckId);
        Truck truck = truckOptional.orElseThrow(() -> new RuntimeException("truck not found"));

        if (!truck.getAvailable()) {
            throw new RuntimeException("truck is not available: [" + truck.getAvailabilityReason() + "]");
        }

        // save to database
        truck.setAvailable(false);
        truck.setAvailabilityReason(claimDetails);
        truckRepo.update(truck);
        log.info(" >>> truck saved to database: [{}]", truck);

        publishAvailability(truckId, false);

        return truck;
    }

    public void releaseTruck(int truckId, String releaseDetails, int currentMiles) {

        Optional<Truck> truckOptional = truckRepo.findById(truckId);
        Truck truck = truckOptional.orElseThrow(() -> new RuntimeException("truck not found"));

        if (truck.getAvailable()) {
            log.info("truck is already available: [{}]", truck);
            return;
        }

        truck.setAvailable(true);
        truck.setAvailabilityReason(releaseDetails);
        truck.setMiles(currentMiles);

        truckRepo.update(truck);
        log.info(" >>> truck saved to database: [{}]", truck);

        publishAvailability(truckId, true);

        inspectIfNeeded(truck);
    }

    private void inspectIfNeeded(Truck truck) {

        if (truck.getMiles() - truck.getLastInspectionMiles() < 2_000) {
            return;
        }

        sendTruckForInspection(truck.getTruckId());
    }

    private void publishAvailability(int truckId, boolean available) {

        TruckAvailabilityChanged availabilityChanged = TruckAvailabilityChanged.builder()
                .truckId(truckId)
                .available(available)
                .build();

        domainEventPublisher.publish(availabilityChanged);
        log.info(" >>> truck availability event published [{}]", availabilityChanged);
    }
}
