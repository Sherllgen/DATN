package com.project.evgo.user.internal;

import com.project.evgo.user.response.VehicleResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Converter for Vehicle entity to DTO.
 */
@Component
public class VehicleDtoConverter {

    public VehicleResponse convert(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .userId(vehicle.getUserId())
                .brand(vehicle.getBrand())
                .modelName(vehicle.getModelName())
                .connectorTypes(vehicle.getConnectorTypes())
                .inUse(vehicle.getInUse())
                .build();
    }

    public List<VehicleResponse> convert(List<Vehicle> vehicles) {
        return vehicles.stream().map(this::convert).toList();
    }

    public Optional<VehicleResponse> convertOptional(Optional<Vehicle> vehicle) {
        return vehicle.map(this::convert);
    }
}
