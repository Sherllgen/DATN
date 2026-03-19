package com.project.evgo.station;

import com.project.evgo.sharedkernel.enums.StationStatus;
import com.project.evgo.station.internal.StationDtoConverter;
import com.project.evgo.station.internal.StationProjection;
import com.project.evgo.station.internal.StationRepository;
import com.project.evgo.station.internal.StationServiceImpl;
import com.project.evgo.station.request.StationFilterRequest;
import com.project.evgo.station.response.StationMetadataResponse;
import com.project.evgo.station.response.StationSearchResult;
import com.project.evgo.sharedkernel.dto.PageResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Station Filter & Metadata features.
 * Written BEFORE implementation (TDD - RED phase).
 */
@ExtendWith(MockitoExtension.class)
class StationFilterServiceTest {

    @InjectMocks
    private StationServiceImpl stationService;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private StationDtoConverter stationDtoConverter;

    // ==================== METADATA TESTS ====================

    @Nested
    @DisplayName("Get Station Metadata Tests")
    class GetMetadataTests {

        @Test
        @DisplayName("Should return correct metadata with power range, connector types, and statuses")
        void getMetadata_ReturnsCorrectMetadata() {
            // Given
            when(stationRepository.findMinPowerOutput()).thenReturn(3.0);
            when(stationRepository.findMaxPowerOutput()).thenReturn(22.0);
            when(stationRepository.findDistinctConnectorTypes())
                    .thenReturn(List.of("IEC_TYPE_2", "VINFAST_STD"));

            // When
            StationMetadataResponse result = stationService.getMetadata();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.minPower()).isEqualTo(3.0);
            assertThat(result.maxPower()).isEqualTo(22.0);
            assertThat(result.connectorTypes()).containsExactly("IEC_TYPE_2", "VINFAST_STD");
            assertThat(result.statuses()).isNotEmpty();
            assertThat(result.statuses()).contains(StationStatus.ACTIVE.name());
            verify(stationRepository).findMinPowerOutput();
            verify(stationRepository).findMaxPowerOutput();
            verify(stationRepository).findDistinctConnectorTypes();
        }

        @Test
        @DisplayName("Should return zero power values when no charger data exists")
        void getMetadata_NoPowerData_ReturnsZeroes() {
            // Given
            when(stationRepository.findMinPowerOutput()).thenReturn(null);
            when(stationRepository.findMaxPowerOutput()).thenReturn(null);
            when(stationRepository.findDistinctConnectorTypes()).thenReturn(List.of());

            // When
            StationMetadataResponse result = stationService.getMetadata();

            // Then
            assertThat(result).isNotNull();
            assertThat(result.minPower()).isEqualTo(0.0);
            assertThat(result.maxPower()).isEqualTo(0.0);
            assertThat(result.connectorTypes()).isEmpty();
            assertThat(result.statuses()).isNotEmpty();
        }
    }

    // ==================== FILTER STATION TESTS ====================
    // Note: service dispatches to findFilteredStations (no connectorTypes)
    //       or findFilteredStationsWithConnectors (when connectorTypes present).

    @Nested
    @DisplayName("Filter Stations Tests")
    class FilterStationsTests {

        @Test
        @DisplayName("Should return all active+inactive stations when no filter params are provided")
        void filterStations_NoParams_ReturnsAll() {
            // Given
            StationFilterRequest request = new StationFilterRequest(null, null, null, null, null, null, null, null, null);

            StationProjection projection = mock(StationProjection.class);
            List<StationProjection> projections = List.of(projection);
            Page<StationProjection> page = new PageImpl<>(projections, PageRequest.of(0, 10), 1);

            // No connectorTypes → dispatches to findFilteredStations
            when(stationRepository.findFilteredStations(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(page);
            when(stationDtoConverter.convertToSearchResults(projections))
                    .thenReturn(List.of(StationSearchResult.builder().id(1L).name("Station A").build()));

            // When
            PageResponse<StationSearchResult> result = stationService.filterStations(request);

            // Then
            assertThat(result.content()).hasSize(1);
            verify(stationRepository).findFilteredStations(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
            verify(stationRepository, never()).findFilteredStationsWithConnectors(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should filter only by status when only status is provided")
        void filterStations_WithStatus_ReturnsFiltered() {
            // Given
            StationFilterRequest request = new StationFilterRequest(null, null, null, StationStatus.ACTIVE, null, null, null, null, null);

            StationProjection projection = mock(StationProjection.class);
            List<StationProjection> projections = List.of(projection);
            Page<StationProjection> page = new PageImpl<>(projections, PageRequest.of(0, 10), 1);

            when(stationRepository.findFilteredStations(eq(null), eq(null), eq("ACTIVE"), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(page);
            when(stationDtoConverter.convertToSearchResults(projections))
                    .thenReturn(List.of(StationSearchResult.builder().id(1L).name("Active Station").build()));

            // When
            PageResponse<StationSearchResult> result = stationService.filterStations(request);

            // Then
            assertThat(result.content()).hasSize(1);
            verify(stationRepository).findFilteredStations(eq(null), eq(null), eq("ACTIVE"), eq(null), eq(null), eq(null), any(Pageable.class));
            verify(stationRepository, never()).findFilteredStationsWithConnectors(any(), any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should filter by power range using the simple (no-connector) query")
        void filterStations_WithPowerRange_ReturnsFiltered() {
            // Given
            StationFilterRequest request = new StationFilterRequest(3.0, 22.0, null, null, null, null, null, null, null);

            StationProjection projection = mock(StationProjection.class);
            List<StationProjection> projections = List.of(projection);
            Page<StationProjection> page = new PageImpl<>(projections, PageRequest.of(0, 10), 1);

            when(stationRepository.findFilteredStations(eq(3.0), eq(22.0), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(page);
            when(stationDtoConverter.convertToSearchResults(projections))
                    .thenReturn(List.of(StationSearchResult.builder().id(2L).name("Power Station").build()));

            // When
            PageResponse<StationSearchResult> result = stationService.filterStations(request);

            // Then
            assertThat(result.content()).hasSize(1);
            verify(stationRepository).findFilteredStations(eq(3.0), eq(22.0), eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
        }

        @Test
        @DisplayName("Should use connector-aware query when connectorTypes list is provided")
        void filterStations_WithConnectorTypes_ReturnsFiltered() {
            // Given
            List<String> connectorTypes = List.of("IEC_TYPE_2", "VINFAST_STD");
            StationFilterRequest request = new StationFilterRequest(null, null, connectorTypes, null, null, null, null, null, null);

            StationProjection projection = mock(StationProjection.class);
            List<StationProjection> projections = List.of(projection);
            Page<StationProjection> page = new PageImpl<>(projections, PageRequest.of(0, 10), 1);

            // Has connectorTypes → dispatches to findFilteredStationsWithConnectors
            when(stationRepository.findFilteredStationsWithConnectors(eq(null), eq(null), eq(connectorTypes), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(page);
            when(stationDtoConverter.convertToSearchResults(projections))
                    .thenReturn(List.of(StationSearchResult.builder().id(3L).name("Type2 Station").build()));

            // When
            PageResponse<StationSearchResult> result = stationService.filterStations(request);

            // Then
            assertThat(result.content()).hasSize(1);
            verify(stationRepository).findFilteredStationsWithConnectors(eq(null), eq(null), eq(connectorTypes), eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
                verify(stationRepository, never()).findFilteredStations(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should apply all filters using connector-aware query when all params are provided")
        void filterStations_AllParams_ReturnsFiltered() {
            // Given
            List<String> connectorTypes = List.of("IEC_TYPE_2");
            StationFilterRequest request = new StationFilterRequest(3.0, 22.0, connectorTypes, StationStatus.ACTIVE, null, null, "Hanoi", null, null);

            StationProjection projection = mock(StationProjection.class);
            List<StationProjection> projections = List.of(projection);
            Page<StationProjection> page = new PageImpl<>(projections, PageRequest.of(0, 10), 1);

            when(stationRepository.findFilteredStationsWithConnectors(eq(3.0), eq(22.0), eq(connectorTypes), eq("ACTIVE"), eq("Hanoi"), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(page);
            when(stationDtoConverter.convertToSearchResults(projections))
                    .thenReturn(List.of(StationSearchResult.builder().id(5L).name("Best Match").build()));

            // When
            PageResponse<StationSearchResult> result = stationService.filterStations(request);

            // Then
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0).name()).isEqualTo("Best Match");
            verify(stationRepository).findFilteredStationsWithConnectors(eq(3.0), eq(22.0), eq(connectorTypes), eq("ACTIVE"), eq("Hanoi"), eq(null), eq(null), any(Pageable.class));
        }

        @Test
        @DisplayName("Should return empty list when no stations match the filter")
        void filterStations_NoMatch_ReturnsEmptyList() {
            // Given
            StationFilterRequest request = new StationFilterRequest(999.0, 9999.0, null, null, null, null, null, null, null);
            Page<StationProjection> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

            when(stationRepository.findFilteredStations(eq(999.0), eq(9999.0), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(page);
            when(stationDtoConverter.convertToSearchResults(List.of()))
                    .thenReturn(List.of());

            // When
            PageResponse<StationSearchResult> result = stationService.filterStations(request);

            // Then
            assertThat(result.content()).isEmpty();
        }

        @Test
        @DisplayName("Should treat empty connectorTypes list as null (uses simple query, no connector filter)")
        void filterStations_EmptyConnectorTypes_TreatsAsNullFilter() {
            // Given — empty list should be normalised to null before calling the repo
            StationFilterRequest request = new StationFilterRequest(null, null, List.of(), null, null, null, null, null, null);

            StationProjection projection = mock(StationProjection.class);
            List<StationProjection> projections = List.of(projection);
            Page<StationProjection> page = new PageImpl<>(projections, PageRequest.of(0, 10), 1);

            // Empty list → normalised to null → uses findFilteredStations (not the connector variant)
            when(stationRepository.findFilteredStations(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                    .thenReturn(page);
            when(stationDtoConverter.convertToSearchResults(projections))
                    .thenReturn(List.of(StationSearchResult.builder().id(1L).build()));

            // When
            PageResponse<StationSearchResult> result = stationService.filterStations(request);

            // Then
            assertThat(result.content()).hasSize(1);
            verify(stationRepository).findFilteredStations(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
            verify(stationRepository, never()).findFilteredStationsWithConnectors(any(), any(), any(), any(), any(), any(), any(), any());
        }
    }
}
