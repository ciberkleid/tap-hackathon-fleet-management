package com.wehaul.fleet.api;

import com.wehaul.fleet.domain.FleetService;
import com.wehaul.fleet.domain.NewTruck;
import com.wehaul.fleet.domain.Truck;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@OpenAPIDefinition(
        info = @Info(
                title = "Fleet Management API",
                version = "1.0"
        ),
        tags = @Tag(name = "Fleet REST API"))
@CrossOrigin
@RestController
@RequestMapping("/api/fleet")
@RequiredArgsConstructor
@Slf4j
public class FleetController {

    private final FleetService fleetService;

    @Operation(summary = "Creates a new truck in the fleet.", method = "POST", tags = "truck")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Truck successfully saved.",
                    headers = @Header(
                            name = "Location",
                            description = "Contains path which can be used to retrieve saved truck. Last element is it's ID.",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Truck data is invalid."
            )
    })
    @PostMapping("/trucks")
    ResponseEntity<Void> create(@Valid @RequestBody TruckCreateRequest request) {

        NewTruck newTruck = fromRequest(request);
        Truck createdTruck = fleetService.addTruck(newTruck);
        URI locationUri = toLocationUri(createdTruck.getTruckId());
        return ResponseEntity.created(locationUri).build();
    }

    @Operation(summary = "Get all trucks in the fleet.", method = "GET", tags = "truck")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of trucks."
            )
    })
    @GetMapping("/trucks")
    List<TruckResponse> getTrucks() {
        return fleetService.getTrucks()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Operation(summary = "Send truck to inspection.", method = "PUT", tags = "truck")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Truck successfully sent to inspection."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Truck cannot be sent to inspection."
            )
    })
    @PutMapping("/trucks/send-for-inspection")
    ResponseEntity<?> sendTruckForInspection(@Valid @RequestBody SendTruckForInspectionRequest request) {
        try {
            fleetService.sendTruckForInspection(request.truckId());
            return ResponseEntity.ok().build();
        }
        catch (RuntimeException exception) {
            log.error(" >>> send truck for inspection failed", exception);
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }


    @Operation(summary = "Return truck from inspection.", method = "PUT", tags = "truck")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Truck successfully returned from inspection."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Truck not found."
            )
    })
    @PutMapping("/trucks/return-from-inspection")
    ResponseEntity<?> returnTruckFromInspection(@Valid @RequestBody ReturnTruckFromInspectionRequest request) {
        try {
            fleetService.returnTruckFromInspection(request.truckId());
            return ResponseEntity.ok().build();
        }
        catch (RuntimeException exception) {
            log.error(" >>> return truck failed", exception);
            return ResponseEntity.notFound().build();
        }
    }

    private TruckResponse toResponse(Truck truck) {
        return new TruckResponse(
                truck.getTruckId(),
                truck.getMiles(),
                truck.getLastInspectionMiles(),
                truck.getAvailable(),
                truck.getAvailabilityReason()
        );
    }

    private NewTruck fromRequest(TruckCreateRequest request) {
        return new NewTruck(request.miles());
    }

    private URI toLocationUri(int id) {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }

    @GetMapping("/hello")
    String hello() {
        log.info("In hello");
        return "hello there";
    }
}
