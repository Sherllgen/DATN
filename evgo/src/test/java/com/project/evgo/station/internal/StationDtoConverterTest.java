package com.project.evgo.station.internal;

import com.project.evgo.station.ChargerStatisticProvider;
import com.project.evgo.station.PortCountProvider;
import com.project.evgo.station.PortCounts;
import com.project.evgo.station.StationPortSummary;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.response.StationOpeningHoursResponse;
import com.project.evgo.station.response.StationResponse;
import com.project.evgo.station.response.StationSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationDtoConverterTest {

    @Mock
    private PortCountProvider portCountProvider;

    @Mock
    private ChargerStatisticProvider chargerStatisticProvider;

    @Mock
    private StationPhotoRepository stationPhotoRepository;

    @InjectMocks
    private StationDtoConverter stationDtoConverter;

    private Station mockStation;

    @BeforeEach
    void setUp() {
        lenient().when(stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(anyLong())).thenReturn(List.of());

        mockStation = new Station();
        mockStation.setId(1L);
        mockStation.setOwnerId(10L);
        mockStation.setName("Test Station");
        mockStation.setDescription("A highly reliable test station.");
        mockStation.setAddress("123 Test Ave");
        mockStation.setLatitude(10.762622);
        mockStation.setLongitude(106.660172);
        mockStation.setRate(4.5);
        mockStation.setStatus(StationStatus.ACTIVE);
        mockStation.setImageUrls(List.of("img1.png", "img2.png"));
        mockStation.setIsFlaggedLowQuality(false);
        mockStation.setCreatedAt(LocalDateTime.now());
        mockStation.setUpdatedAt(LocalDateTime.now());

        StationOpeningHours hours = new StationOpeningHours();
        hours.setId(100L);
        hours.setStation(mockStation);
        hours.setDayOfWeek(DayOfWeek.MONDAY);
        hours.setOpenTime(LocalTime.of(8, 0));
        hours.setCloseTime(LocalTime.of(22, 0));
        hours.setIsOpen(true);

        mockStation.setOpeningHours(List.of(hours));
    }

    @Test
    void convert_ShouldReturnStationResponse_WithCorrectFields() {
        when(portCountProvider.getPortCounts(1L)).thenReturn(new PortCounts(10, 5));
        when(chargerStatisticProvider.getTotalChargerCount(1L)).thenReturn(4L);
        when(chargerStatisticProvider.getAvailableChargerCount(1L)).thenReturn(2L);
        when(portCountProvider.getPortSummaries(1L)).thenReturn(List.of(
                new StationPortSummary(ConnectorType.IEC_TYPE_2, 2, 4)
        ));

        StationResponse response = stationDtoConverter.convert(mockStation);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(10L, response.ownerId());
        assertEquals("Test Station", response.name());
        assertEquals(10, response.totalPorts());
        assertEquals(5, response.availablePorts());
        assertEquals(4, response.totalChargersCount());
        assertEquals(2, response.availableChargersCount());
        
        assertEquals(1, response.chargers().size());
        assertEquals("IEC_TYPE_2", response.chargers().get(0).connectorType());
        assertEquals(4, response.chargers().get(0).total());
        assertEquals(2, response.chargers().get(0).available());

        assertNotNull(response.openingHours());
        assertEquals(1, response.openingHours().size());
        StationOpeningHoursResponse hoursResponse = response.openingHours().get(0);
        assertEquals(DayOfWeek.MONDAY, hoursResponse.dayOfWeek());
    }

    @Test
    void convertList_ShouldReturnListOfStationResponses() {
        when(portCountProvider.getPortCounts(1L)).thenReturn(new PortCounts(10, 5));
        when(chargerStatisticProvider.getTotalChargerCount(1L)).thenReturn(4L);
        when(chargerStatisticProvider.getAvailableChargerCount(1L)).thenReturn(2L);
        when(portCountProvider.getPortSummaries(1L)).thenReturn(List.of());

        List<StationResponse> responses = stationDtoConverter.convert(List.of(mockStation));

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).id());
    }

    @Test
    void convertOptional_ShouldReturnOptionalStationResponse() {
        when(portCountProvider.getPortCounts(1L)).thenReturn(new PortCounts(10, 5));
        when(chargerStatisticProvider.getTotalChargerCount(1L)).thenReturn(4L);
        when(chargerStatisticProvider.getAvailableChargerCount(1L)).thenReturn(2L);
        when(portCountProvider.getPortSummaries(1L)).thenReturn(List.of());

        Optional<StationResponse> responseOpt = stationDtoConverter.convert(Optional.of(mockStation));

        assertTrue(responseOpt.isPresent());
        assertEquals(1L, responseOpt.get().id());
    }
    
    @Test
    void convertOptional_ShouldReturnEmpty_WhenGivenEmptyOptional() {
        Optional<StationResponse> responseOpt = stationDtoConverter.convert(Optional.empty());
        assertTrue(responseOpt.isEmpty());
    }

    @Test
    void toResponse_ShouldAliasConvert() {
        when(portCountProvider.getPortCounts(1L)).thenReturn(new PortCounts(10, 5));
        when(chargerStatisticProvider.getTotalChargerCount(1L)).thenReturn(4L);
        when(chargerStatisticProvider.getAvailableChargerCount(1L)).thenReturn(2L);
        when(portCountProvider.getPortSummaries(1L)).thenReturn(List.of());

        StationResponse response = stationDtoConverter.toResponse(mockStation);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void convertToSearchResults_ShouldReturnListOfSearchResponses() {
        StationProjection projection = mock(StationProjection.class);
        when(projection.getId()).thenReturn(1L);
        when(projection.getName()).thenReturn("Test Station");
        when(projection.getAddress()).thenReturn("123 Test Ave");
        when(projection.getLatitude()).thenReturn(10.762622);
        when(projection.getLongitude()).thenReturn(106.660172);
        when(projection.getRate()).thenReturn(4.5);
        when(projection.getStatus()).thenReturn(StationStatus.ACTIVE);
        when(projection.getIsFlaggedLowQuality()).thenReturn(false);
        when(projection.getDistance()).thenReturn(1500.0);
        when(projection.getTotalChargersCount()).thenReturn(4);
        when(projection.getAvailableChargersCount()).thenReturn(2);

        List<StationSearchResult> results = stationDtoConverter.convertToSearchResults(List.of(projection));

        assertNotNull(results);
        assertEquals(1, results.size());
        StationSearchResult result = results.get(0);
        assertEquals(1L, result.id());
        assertEquals("Test Station", result.name());
        assertEquals(1.5, result.distanceKm()); // 1500m -> 1.5km
        assertEquals(4, result.totalChargersCount());
        assertEquals(2, result.availableChargersCount());
    }
    
    @Test
    void convertToSearchResults_ShouldHandleNullOrEmpty() {
        assertTrue(stationDtoConverter.convertToSearchResults(null).isEmpty());
        assertTrue(stationDtoConverter.convertToSearchResults(List.of()).isEmpty());
    }
    
    @Test
    void convertToSearchResults_ShouldHandleNullCountsAndDistance() {
        StationProjection projection = mock(StationProjection.class);
        when(projection.getDistance()).thenReturn(null);
        when(projection.getTotalChargersCount()).thenReturn(null);
        when(projection.getAvailableChargersCount()).thenReturn(null);
        
        List<StationSearchResult> results = stationDtoConverter.convertToSearchResults(List.of(projection));
        
        assertNotNull(results);
        assertEquals(1, results.size());
        StationSearchResult result = results.get(0);
        assertNull(result.distanceKm());
        assertEquals(0, result.totalChargersCount());
        assertEquals(0, result.availableChargersCount());
    }

    @Test
    void convert_ShouldReturnStationResponse_WithImageUrlsFromPhotos() {
        when(portCountProvider.getPortCounts(1L)).thenReturn(new PortCounts(10, 5));
        when(chargerStatisticProvider.getTotalChargerCount(1L)).thenReturn(4L);
        when(chargerStatisticProvider.getAvailableChargerCount(1L)).thenReturn(2L);
        when(portCountProvider.getPortSummaries(1L)).thenReturn(List.of());

        StationPhoto photo1 = new StationPhoto();
        photo1.setImageUrl("photo1.png");
        StationPhoto photo2 = new StationPhoto();
        photo2.setImageUrl("photo2.png");
        
        when(stationPhotoRepository.findByStationIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(photo1, photo2));

        StationResponse response = stationDtoConverter.convert(mockStation);

        assertNotNull(response);
        assertEquals(2, response.imageUrls().size());
        assertEquals("photo1.png", response.imageUrls().get(0));
        assertEquals("photo2.png", response.imageUrls().get(1));
    }
}
