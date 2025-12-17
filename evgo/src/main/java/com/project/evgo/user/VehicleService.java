package com.project.evgo.user;

import com.project.evgo.user.request.CreateVehicleRequest;
import com.project.evgo.user.request.UpdateVehicleRequest;
import com.project.evgo.user.response.VehicleResponse;

import java.util.List;

/**
 * Service interface for managing vehicles.
 */
public interface VehicleService {
    VehicleResponse addVehicle(CreateVehicleRequest request);

    List<VehicleResponse> getMyVehicles();

    VehicleResponse updateVehicle(Long vehicleId, UpdateVehicleRequest request);

    void deleteVehicle(Long vehicleId);

    VehicleResponse getVehicleById(Long vehicleId);

    VehicleResponse setVehicleInUse(Long vehicleId);

    VehicleResponse getInUseVehicle();
}
