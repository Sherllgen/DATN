package com.project.evgo.charger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.evgo.charger.internal.web.ChargerController;
import com.project.evgo.charger.request.CreateChargerRequest;
import com.project.evgo.charger.request.CreatePortRequest;
import com.project.evgo.charger.request.UpdateChargerRequest;
import com.project.evgo.charger.request.UpdatePortRequest;
import com.project.evgo.charger.response.ChargerResponse;
import com.project.evgo.charger.response.PortResponse;
import com.project.evgo.sharedkernel.enums.ChargerStatus;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.sharedkernel.enums.ErrorCode;
import com.project.evgo.sharedkernel.enums.PortStatus;
import com.project.evgo.sharedkernel.exceptions.AppException;
import com.project.evgo.sharedkernel.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ChargerController.
 */
@ExtendWith(MockitoExtension.class)
class ChargerControllerTest {

	private MockMvc mockMvc;

	@Mock
	private ChargerService chargerService;

	@InjectMocks
	private ChargerController chargerController;

	private ObjectMapper objectMapper;

	private static final Long STATION_ID = 100L;
	private static final Long CHARGER_ID = 200L;
	private static final Long PORT_ID = 300L;

	private ChargerResponse testChargerResponse;
	private PortResponse testPortResponse;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(chargerController)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
		objectMapper = new ObjectMapper();

		testChargerResponse = ChargerResponse.builder()
				.id(CHARGER_ID)
				.name("Test Charger")
				.maxPower(50.0)
				.connectorType(ConnectorType.VINFAST_STD)
				.status(ChargerStatus.AVAILABLE)
				.stationId(STATION_ID)
				.build();

		testPortResponse = PortResponse.builder()
				.id(PORT_ID)
				.portNumber(1)
				.status(PortStatus.AVAILABLE)
				.chargerId(CHARGER_ID)
				.build();
	}

	// ==================== GET CHARGERS TESTS ====================

	@Nested
	@DisplayName("GET /api/v1/chargers Tests")
	class GetChargersTests {

		@Test
		@DisplayName("Should return chargers for station")
		void getByStationId_ReturnsChargers() throws Exception {
			when(chargerService.findByStationId(STATION_ID))
					.thenReturn(List.of(testChargerResponse));

			mockMvc.perform(get("/api/v1/chargers")
					.param("stationId", STATION_ID.toString()))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.data").isArray())
					.andExpect(jsonPath("$.data[0].id").value(CHARGER_ID));
		}

		@Test
		@DisplayName("Should return charger by ID")
		void getById_ReturnsCharger() throws Exception {
			when(chargerService.findById(CHARGER_ID))
					.thenReturn(Optional.of(testChargerResponse));

			mockMvc.perform(get("/api/v1/chargers/{id}", CHARGER_ID))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.data.id").value(CHARGER_ID))
					.andExpect(jsonPath("$.data.name").value("Test Charger"));
		}

		@Test
		@DisplayName("Should return 404 when charger not found")
		void getById_NotFound_Returns404() throws Exception {
			when(chargerService.findById(CHARGER_ID))
					.thenReturn(Optional.empty());

			mockMvc.perform(get("/api/v1/chargers/{id}", CHARGER_ID))
					.andExpect(status().isNotFound());
		}
	}

	// ==================== CREATE CHARGER TESTS ====================

	@Nested
	@DisplayName("POST /api/v1/chargers Tests")
	class CreateChargerTests {

		@Test
		@DisplayName("Should create charger successfully")
		void create_ValidRequest_ReturnsCreated() throws Exception {
			CreateChargerRequest request = new CreateChargerRequest();
			request.setName("New Charger");
			request.setMaxPower(50.0);
			request.setStationId(STATION_ID);
			request.setConnectorType(ConnectorType.VINFAST_STD);

			when(chargerService.createCharger(any(CreateChargerRequest.class)))
					.thenReturn(testChargerResponse);

			mockMvc.perform(post("/api/v1/chargers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.status").value(201))
					.andExpect(jsonPath("$.data.id").value(CHARGER_ID));
		}

		@Test
		@DisplayName("Should return 403 when not owner")
		void create_NotOwner_Returns403() throws Exception {
			CreateChargerRequest request = new CreateChargerRequest();
			request.setName("New Charger");
			request.setMaxPower(50.0);
			request.setStationId(STATION_ID);
			request.setConnectorType(ConnectorType.VINFAST_STD);

			when(chargerService.createCharger(any(CreateChargerRequest.class)))
					.thenThrow(new AppException(ErrorCode.STATION_NOT_OWNED));

			mockMvc.perform(post("/api/v1/chargers")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isForbidden());
		}
	}

	// ==================== UPDATE CHARGER TESTS ====================

	@Nested
	@DisplayName("PUT /api/v1/chargers/{id} Tests")
	class UpdateChargerTests {

		@Test
		@DisplayName("Should update charger successfully")
		void update_ValidRequest_ReturnsUpdated() throws Exception {
			UpdateChargerRequest request = new UpdateChargerRequest(
					"Updated Charger", 100.0, ConnectorType.IEC_TYPE_2);

			when(chargerService.updateCharger(eq(CHARGER_ID), any(UpdateChargerRequest.class)))
					.thenReturn(testChargerResponse);

			mockMvc.perform(put("/api/v1/chargers/{id}", CHARGER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.data.id").value(CHARGER_ID));
		}

		@Test
		@DisplayName("Should return 404 when charger not found")
		void update_NotFound_Returns404() throws Exception {
			UpdateChargerRequest request = new UpdateChargerRequest(
					"Updated Charger", 100.0, ConnectorType.IEC_TYPE_2);

			when(chargerService.updateCharger(eq(CHARGER_ID), any(UpdateChargerRequest.class)))
					.thenThrow(new AppException(ErrorCode.CHARGER_NOT_FOUND));

			mockMvc.perform(put("/api/v1/chargers/{id}", CHARGER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isNotFound());
		}
	}

	// ==================== DELETE CHARGER TESTS ====================

	@Nested
	@DisplayName("DELETE /api/v1/chargers/{id} Tests")
	class DeleteChargerTests {

		@Test
		@DisplayName("Should delete charger successfully")
		void delete_ValidOwner_ReturnsOk() throws Exception {
			doNothing().when(chargerService).deleteCharger(CHARGER_ID);

			mockMvc.perform(delete("/api/v1/chargers/{id}", CHARGER_ID))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.status").value(200))
					.andExpect(jsonPath("$.message").value("Charger deleted successfully"));
		}

		@Test
		@DisplayName("Should return 403 when not owner")
		void delete_NotOwner_Returns403() throws Exception {
			doThrow(new AppException(ErrorCode.CHARGER_NOT_OWNED))
					.when(chargerService).deleteCharger(CHARGER_ID);

			mockMvc.perform(delete("/api/v1/chargers/{id}", CHARGER_ID))
					.andExpect(status().isForbidden());
		}
	}

	// ==================== PORT TESTS ====================

	@Nested
	@DisplayName("Port Endpoints Tests")
	class PortTests {

		@Test
		@DisplayName("Should get ports by charger ID")
		void getPortsByChargerId_ReturnsPorts() throws Exception {
			when(chargerService.findPortsByChargerId(CHARGER_ID))
					.thenReturn(List.of(testPortResponse));

			mockMvc.perform(get("/api/v1/chargers/{id}/ports", CHARGER_ID))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data").isArray())
					.andExpect(jsonPath("$.data[0].id").value(PORT_ID));
		}

		@Test
		@DisplayName("Should create port successfully")
		void createPort_ValidRequest_ReturnsCreated() throws Exception {
			CreatePortRequest request = new CreatePortRequest(1);

			when(chargerService.createPort(eq(CHARGER_ID), any(CreatePortRequest.class)))
					.thenReturn(testPortResponse);

			mockMvc.perform(post("/api/v1/chargers/{chargerId}/ports", CHARGER_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.data.id").value(PORT_ID));
		}

		@Test
		@DisplayName("Should update port status successfully")
		void updatePortStatus_ValidRequest_ReturnsUpdated() throws Exception {
			UpdatePortRequest request = new UpdatePortRequest(PortStatus.CHARGING);

			when(chargerService.updatePortStatus(eq(PORT_ID), any(PortStatus.class)))
					.thenReturn(testPortResponse);

			mockMvc.perform(patch("/api/v1/chargers/ports/{portId}/status", PORT_ID)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.data.id").value(PORT_ID));
		}

		@Test
		@DisplayName("Should delete port successfully")
		void deletePort_ValidOwner_ReturnsOk() throws Exception {
			doNothing().when(chargerService).deletePort(PORT_ID);

			mockMvc.perform(delete("/api/v1/chargers/ports/{portId}", PORT_ID))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.message").value("Port deleted successfully"));
		}
	}
}
