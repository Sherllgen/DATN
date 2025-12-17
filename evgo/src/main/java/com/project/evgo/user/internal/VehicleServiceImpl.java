package com.project.evgo.user.internal;

import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.user.VehicleService;
import com.project.evgo.user.request.CreateVehicleRequest;
import com.project.evgo.user.request.UpdateVehicleRequest;
import com.project.evgo.user.response.VehicleResponse;
import com.project.evgo.user.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of VehicleService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleDtoConverter vehicleDtoConverter;

    @Override
    @Transactional
    public VehicleResponse addVehicle(CreateVehicleRequest request) {
        Long currentUserId = getCurrentUserId();

        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(currentUserId);
        vehicle.setBrand(request.brand());
        vehicle.setModelName(request.modelName());
        vehicle.setConnectorTypes(request.connectorTypes());

        vehicleRepository.save(vehicle);
        log.info("Vehicle created for user {}: {}", currentUserId, vehicle.getId());

        return vehicleDtoConverter.convert(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehicleResponse> getMyVehicles() {
        Long currentUserId = getCurrentUserId();
        List<Vehicle> vehicles = vehicleRepository.findAllByUserId(currentUserId);
        return vehicleDtoConverter.convert(vehicles);
    }

    @Override
    @Transactional
    public VehicleResponse updateVehicle(Long vehicleId, UpdateVehicleRequest request) {
        Long currentUserId = getCurrentUserId();
        Vehicle vehicle = getVehicleAndCheckOwnership(vehicleId, currentUserId);

        vehicle.setBrand(request.brand());
        vehicle.setModelName(request.modelName());
        vehicle.setConnectorTypes(request.connectorTypes());

        vehicleRepository.save(vehicle);
        log.info("Vehicle updated: {}", vehicleId);

        return vehicleDtoConverter.convert(vehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long vehicleId) {
        Long currentUserId = getCurrentUserId();
        Vehicle vehicle = getVehicleAndCheckOwnership(vehicleId, currentUserId);

        vehicleRepository.delete(vehicle);
        log.info("Vehicle deleted: {}", vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long vehicleId) {
        Long currentUserId = getCurrentUserId();
        Vehicle vehicle = getVehicleAndCheckOwnership(vehicleId, currentUserId);
        return vehicleDtoConverter.convert(vehicle);
    }

    @Override
    @Transactional
    public VehicleResponse setVehicleInUse(Long vehicleId) {
        Long currentUserId = getCurrentUserId();
        Vehicle vehicle = getVehicleAndCheckOwnership(vehicleId, currentUserId);

        // Clear inUse flag for all user's vehicles
        List<Vehicle> userVehicles = vehicleRepository.findAllByUserId(currentUserId);
        for (Vehicle v : userVehicles) {
            v.setInUse(false);
        }
        vehicleRepository.saveAll(userVehicles);

        // Set the selected vehicle as in use
        vehicle.setInUse(true);
        vehicleRepository.save(vehicle);
        log.info("Vehicle {} set as in use for user {}", vehicleId, currentUserId);

        return vehicleDtoConverter.convert(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getInUseVehicle() {
        Long currentUserId = getCurrentUserId();
        return vehicleRepository.findByUserIdAndInUseTrue(currentUserId)
                .map(vehicleDtoConverter::convert)
                .orElse(null);
    }

    private Long getCurrentUserId() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (userId == null) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        return userId;
    }

    private Vehicle getVehicleAndCheckOwnership(Long vehicleId, Long userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Vehicle not found"));

        if (!vehicle.getUserId().equals(userId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "You do not own this vehicle");
        }
        return vehicle;
    }
}
