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

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
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

                        // District 1 - Ben Thanh Market
                        Station benThanh = createStation(
                                        ownerId,
                                        "Ben Thanh EV Station",
                                        "Charging hub near the iconic Ben Thanh Market",
                                        "Le Loi, Ben Thanh Ward, District 1, Ho Chi Minh City",
                                        10.7724,
                                        106.6980,
                                        4.6,
                                        List.of("https://futureev.net/wp-content/uploads/2023/08/future-ev-chariging-station-1024x576.webp",
                                                        "https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg"));

                        // District 1 - Notre Dame Cathedral
                        Station notreDame = createStation(
                                        ownerId,
                                        "Notre Dame EV Hub",
                                        "Premium charging station near historical cathedral",
                                        "01 Cong Xa Paris, Ben Nghe Ward, District 1, Ho Chi Minh City",
                                        10.7797,
                                        106.6990,
                                        4.8,
                                        List.of("https://media.licdn.com/dms/image/D4E22AQEU6KExAdJE0g/feedshare-shrink_800/0/1702310055283?e=2147483647&v=beta&t=mxhcbqfIQSZxMhzTI4mo3HxI3v2GV9PwgXwjXBfmkuU",
                                                        "https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg"));

                        // District 1 - City Hall (People's Committee)
                        Station cityHall = createStation(
                                        ownerId,
                                        "City Hall EV Point",
                                        "Convenient charging at the heart of the city",
                                        "86 Le Thanh Ton, Ben Nghe Ward, District 1, Ho Chi Minh City",
                                        10.7767,
                                        106.7004,
                                        4.5,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg",
                                                        "https://futureev.net/wp-content/uploads/2023/08/future-ev-chariging-station-1024x576.webp"));

                        // District 1 - Opera House (Municipal Theatre)
                        Station operaHouse = createStation(
                                        ownerId,
                                        "Opera House Charging Plaza",
                                        "Fast charging near the historic Saigon Opera House",
                                        "07 Lam Son Square, Ben Nghe Ward, District 1, Ho Chi Minh City",
                                        10.7770,
                                        106.7033,
                                        4.7,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg",
                                                        "https://media.licdn.com/dms/image/D4E22AQEU6KExAdJE0g/feedshare-shrink_800/0/1702310055283?e=2147483647&v=beta&t=mxhcbqfIQSZxMhzTI4mo3HxI3v2GV9PwgXwjXBfmkuU"));

                        // District 1 - Nguyen Hue Walking Street
                        Station nguyenHue = createStation(
                                        ownerId,
                                        "Nguyen Hue EV Center",
                                        "Urban charging station on the famous walking street",
                                        "Nguyen Hue, Ben Nghe Ward, District 1, Ho Chi Minh City",
                                        10.7743,
                                        106.7020,
                                        4.4,
                                        List.of("https://futureev.net/wp-content/uploads/2023/08/future-ev-chariging-station-1024x576.webp",
                                                        "https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg"));

                        // District 1 - Book Street
                        Station bookStreet = createStation(
                                        ownerId,
                                        "Book Street Smart Charger",
                                        "Eco-friendly charging near cultural hub",
                                        "Nguyen Van Binh, Ben Nghe Ward, District 1, Ho Chi Minh City",
                                        10.7796,
                                        106.7018,
                                        4.3,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg",
                                                        "https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg"));

                        // --- NEW STATIONS: Bình Đại, Bến Tre ---

                        // 1. Bình Đại Central Station
                        Station binhDaiCentral = createStation(
                                        ownerId,
                                        "Bình Đại Central Station",
                                        "Charging station in the heart of Bình Đại town",
                                        "Thị trấn Bình Đại, Huyện Bình Đại, Bến Tre",
                                        10.26713,
                                        106.52038,
                                        4.5,
                                        List.of("https://futureev.net/wp-content/uploads/2023/08/future-ev-chariging-station-1024x576.webp"));

                        // 2. Bình Đại Coastal Hub
                        Station binhDaiCoastal = createStation(
                                        ownerId,
                                        "Bình Đại Coastal Hub",
                                        "Fast charging near the coastal area",
                                        "Khu du lịch biển Bình Đại, Bến Tre",
                                        10.2701299,
                                        106.4973792,
                                        4.8,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2023/02/MRT-EV-04-1024x683.jpg"));

                        // 3. Bình Đại West Point
                        Station binhDaiWest = createStation(
                                        ownerId,
                                        "Bình Đại West Point",
                                        "Convenient stop for travelers heading west",
                                        "Ấp 3, Bình Đại, Bến Tre",
                                        10.2521299,
                                        106.5153792,
                                        4.2,
                                        List.of("https://www.egat.co.th/home/en/wp-content/uploads/2021/12/02.jpg"));

                        // Save stations
                        List<Station> stations = stationRepository
                                        .saveAll(List.of(bitexco, palace, crescent, landmark,
                                                        benThanh, notreDame, cityHall, operaHouse, nguyenHue,
                                                        bookStreet, binhDaiCentral, binhDaiCoastal, binhDaiWest));
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

                        // Ben Thanh Market - 4 chargers (mix of AC and DC)
                        createChargers(stations.get(4).getId(), List.of(
                                        createCharger("BenThanh AC-01", 11.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("BenThanh DC-01", 50.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.IN_USE),
                                        createCharger("BenThanh AC-02", 7.4, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("BenThanh DC-Fast", 150.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE)));

                        // Notre Dame Cathedral - 3 premium chargers
                        createChargers(stations.get(5).getId(), List.of(
                                        createCharger("NotreDame AC-Premium", 22.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("NotreDame DC-01", 150.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("NotreDame DC-02", 150.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.IN_USE)));

                        // City Hall - 2 chargers (one under maintenance)
                        createChargers(stations.get(6).getId(), List.of(
                                        createCharger("CityHall AC-01", 11.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("CityHall DC-Repair", 50.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.MAINTENANCE)));

                        // Opera House - 3 chargers
                        createChargers(stations.get(7).getId(), List.of(
                                        createCharger("Opera AC-01", 22.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("Opera DC-01", 50.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("Opera DC-Fast", 150.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.IN_USE)));

                        // Nguyen Hue - 2 urban chargers
                        createChargers(stations.get(8).getId(), List.of(
                                        createCharger("NguyenHue AC-01", 7.4, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("NguyenHue AC-02", 11.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE)));

                        // Book Street - 3 eco-friendly chargers
                        createChargers(stations.get(9).getId(), List.of(
                                        createCharger("BookSt AC-01", 11.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("BookSt AC-02", 7.4, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("BookSt DC-01", 50.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.MAINTENANCE)));

                        // Seed 3 stations near Binh Dai, Ben Tre (7F2W+RWP)
                        // Coords approximately: 10.19354, 106.64455

                        // 1. Binh Dai Central Station
                        createChargers(stations.get(10).getId(), List.of(
                                        createCharger("BD-Central DC-01", 50.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("BD-Central AC-01", 22.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE)));

                        // 2. Binh Dai Coastal Hub
                        createChargers(stations.get(11).getId(), List.of(
                                        createCharger("BD-Coastal DC-Fast", 150.0, ConnectorType.VINFAST_STD,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("BD-Coastal AC-01", 11.0, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.IN_USE)));

                        // 3. Binh Dai West Point
                        createChargers(stations.get(12).getId(), List.of(
                                        createCharger("BD-West AC-01", 7.4, ConnectorType.IEC_TYPE_2,
                                                        ChargerStatus.AVAILABLE),
                                        createCharger("BD-West AC-02", 7.4, ConnectorType.IEC_TYPE_2,
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

                // Add default 24/7 opening hours
                List<StationOpeningHours> openingHours = createDefaultOpeningHours(station);
                station.setOpeningHours(openingHours);

                return station;
        }

        private List<StationOpeningHours> createDefaultOpeningHours(Station station) {
                List<StationOpeningHours> openingHours = new ArrayList<>();

                // Default: 24/7 operation
                for (DayOfWeek day : DayOfWeek.values()) {
                        StationOpeningHours hours = new StationOpeningHours();
                        hours.setStation(station);
                        hours.setDayOfWeek(day);
                        hours.setOpenTime(null); // null indicates 24/7
                        hours.setCloseTime(null);
                        hours.setIsOpen(true);
                        openingHours.add(hours);
                }

                return openingHours;
        }

        private List<StationOpeningHours> createCustomOpeningHours(Station station, LocalTime openTime,
                        LocalTime closeTime) {
                List<StationOpeningHours> openingHours = new ArrayList<>();

                for (DayOfWeek day : DayOfWeek.values()) {
                        StationOpeningHours hours = new StationOpeningHours();
                        hours.setStation(station);
                        hours.setDayOfWeek(day);
                        hours.setOpenTime(openTime);
                        hours.setCloseTime(closeTime);
                        hours.setIsOpen(true);
                        openingHours.add(hours);
                }

                return openingHours;
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
