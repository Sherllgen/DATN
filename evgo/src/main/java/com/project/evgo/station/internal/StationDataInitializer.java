package com.project.evgo.station.internal;

import com.project.evgo.charger.internal.Charger;
import com.project.evgo.charger.internal.ChargerRepository;
import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.sharedkernel.utils.GeoUtils;
import com.project.evgo.user.internal.User;
import com.project.evgo.user.internal.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Data initializer for Station and Charger entities.
 * Only runs in 'local' and 'dev' profiles.
 * Runs after UserDataInitializer to ensure Station Owner user exists.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class StationDataInitializer implements CommandLineRunner {

        private final StationRepository stationRepository;
        private final ChargerRepository chargerRepository;
        private final UserRepository userRepository;

        @Override
        public void run(String... args) {
                // Skip if data already exists
                if (stationRepository.count() > 0) {
                        log.info("Stations already initialized. Skipping seed data.");
                        return;
                }

                log.info("Initializing station seed data...");

                try {
                        // Get Station Owner user
                        User owner = userRepository.findByEmail("station.owner@evgo.com")
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Station owner user not found. Ensure UserDataInitializer runs first."));
                        Long ownerId = owner.getId();
                        log.info("Using owner ID: {} ({})", ownerId, owner.getEmail());

                        // Create stations
                        Station bitexco = createStation(
                                        ownerId,
                                        "EV-Go Bitexco Central",
                                        "Premium charging station near financial district",
                                        "2 Hai Trieu, Ben Nghe, District 1, Ho Chi Minh City",
                                        10.771918,
                                        106.704439,
                                        4.8,
                                        List.of("https://futureev.net/wp-content/uploads/2023/08/future-ev-chariging-station-1024x576.webp",
                                                        "https://media.licdn.com/dms/image/D4E22AQEU6KExAdJE0g/feedshare-shrink_800/0/1702310055283?e=2147483647&v=beta&t=mxhcbqfIQSZxMhzTI4mo3HxI3v2GV9PwgXwjXBfmkuU"));

                        Station palace = createStation(
                                        ownerId,
                                        "Independence Palace Station",
                                        "Convenient parking and charging spot",
                                        "135 Nam Ky Khoi Nghia, Ben Thanh, District 1, Ho Chi Minh City",
                                        10.776111,
                                        106.695833,
                                        4.5,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg",
                                                        "https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg"));

                        Station crescent = createStation(
                                        ownerId,
                                        "Crescent Mall EV Hub",
                                        "Spacious charging area in Crescent Mall basement",
                                        "101 Ton Dat Tien, Tan Phu, District 7, Ho Chi Minh City",
                                        10.729150,
                                        106.721400,
                                        4.7,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg",
                                                        "https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg"));

                        Station landmark = createStation(
                                        ownerId,
                                        "Landmark 81 Supercharger",
                                        "Fast charging at the tallest building in Vietnam",
                                        "720A Dien Bien Phu, Ward 22, Binh Thanh, Ho Chi Minh City",
                                        10.795000,
                                        106.721900,
                                        4.9,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg",
                                                        "https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg"));

                        // Save stations
                        List<Station> stations = stationRepository
                                        .saveAll(List.of(bitexco, palace, crescent, landmark));
                        log.info("Created {} stations", stations.size());

                        // Create chargers for each station
                        createChargers(stations.get(0).getId(), List.of(
                                        createCharger("Bitexco AC-01", 22.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("Bitexco DC-Rapid", 150.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.IN_USE)));

                        createChargers(stations.get(1).getId(), List.of(
                                        createCharger("Palace AC-01", 11.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE)));

                        createChargers(stations.get(2).getId(), List.of(
                                        createCharger("Crescent AC-01", 7.4, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("Crescent DC-01", 50.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("Crescent DC-Repair", 50.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.MAINTENANCE)));

                        createChargers(stations.get(3).getId(), List.of(
                                        createCharger("Landmark SC-01", 250.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("Landmark SC-02", 250.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE)));

                        log.info("Station and charger seed data initialized successfully!");

                } catch (Exception e) {
                        log.error("Failed to initialize seed data", e);
                }
        }

        private Station createStation(Long ownerId, String name, String description, String address,
                        double latitude, double longitude, double rate, List<String> imageUrls) {
                Station station = new Station();
                station.setOwnerId(ownerId);
                station.setName(name);
                station.setDescription(description);
                station.setAddress(address);
                station.setLatitude(latitude);
                station.setLongitude(longitude);
                station.setLocation(GeoUtils.createPoint(latitude, longitude));
                station.setRate(rate);
                station.setStatus(StationStatus.ACTIVE);
                station.setImageUrls(imageUrls);
                station.setIsFlaggedLowQuality(false);
                return station;
        }

        private Charger createCharger(String name, double maxPower, ConnectorType connectorType, ChargerStatus status) {
                Charger charger = new Charger();
                charger.setName(name);
                charger.setMaxPower(maxPower);
                charger.setConnectorType(connectorType);
                charger.setStatus(status);
                return charger;
        }

        private void createChargers(Long stationId, List<Charger> chargers) {
                chargers.forEach(charger -> charger.setStationId(stationId));
                chargerRepository.saveAll(chargers);
                log.info("Created {} chargers for station {}", chargers.size(), stationId);
        }
}
