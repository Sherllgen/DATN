package com.project.evgo.user.internal.web;

import com.project.evgo.sharedkernel.dto.ApiResponse;
import com.project.evgo.sharedkernel.enums.ConnectorType;
import com.project.evgo.user.response.VehicleCatalogEntry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/**
 * REST Controller for electric motorcycle catalog (public reference data).
 */
@RestController
@RequestMapping("/api/v1/vehicles/catalog")
@Tag(name = "Vehicle Catalog", description = "APIs for electric motorcycle reference data")
public class VehicleCatalogController {

    private static final List<VehicleCatalogEntry> CATALOG = List.of(
        // ========== VinFast ========== 
        new VehicleCatalogEntry("VinFast", "Klara S", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("VinFast", "Feliz S", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("VinFast", "Theon S", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("VinFast", "Vento S", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("VinFast", "Evo 200", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("VinFast", "Ludo", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("VinFast", "Impes", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("VinFast", "Tempest", Set.of(ConnectorType.VINFAST_STD, ConnectorType.SOCKET_220V)),

        // ========== Dat Bike ==========
        new VehicleCatalogEntry("Dat Bike", "Weaver++", Set.of(ConnectorType.DATBIKE_FAST, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Dat Bike", "Quantum", Set.of(ConnectorType.DATBIKE_FAST, ConnectorType.IEC_TYPE_2, ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Dat Bike", "Weaver 200", Set.of(ConnectorType.SOCKET_220V)),

        // ========== Yadea ==========
        new VehicleCatalogEntry("Yadea", "Xmen Neo", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Yadea", "Ulike", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Yadea", "S3 Pro", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Yadea", "Lexin", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Yadea", "Voltguard", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Yadea", "G5", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Yadea", "Zoomer", Set.of(ConnectorType.SOCKET_220V)),

        // ========== Pega ==========
        new VehicleCatalogEntry("Pega", "eSH", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Pega", "NewTech", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Pega", "Cap A2", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Pega", "X-Men", Set.of(ConnectorType.SOCKET_220V)),

        // ========== Selex ==========
        new VehicleCatalogEntry("Selex", "Camel", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Selex", "Luda", Set.of(ConnectorType.SOCKET_220V)),

        // ========== Dibao ==========
        new VehicleCatalogEntry("Dibao", "Nami", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Dibao", "Jeek", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Dibao", "Xega", Set.of(ConnectorType.SOCKET_220V)),

        // ========== MVS ==========
        new VehicleCatalogEntry("MVS", "Melo", Set.of(ConnectorType.SOCKET_220V)),

        // ========== AIMA ==========
        new VehicleCatalogEntry("AIMA", "Windy", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("AIMA", "Xichun", Set.of(ConnectorType.SOCKET_220V)),

        // ========== Gogoro (Changing battery) ==========
        new VehicleCatalogEntry("Gogoro", "Viva", Set.of(ConnectorType.SOCKET_220V)),
        new VehicleCatalogEntry("Gogoro", "Delight", Set.of(ConnectorType.SOCKET_220V)),

        // ========== Other ==========
        new VehicleCatalogEntry("Khác", "Xe máy điện khác", Set.of(ConnectorType.OTHER)));

    @GetMapping
    @Operation(summary = "Get vehicle catalog", description = "Returns a list of popular electric motorcycles with their supported charging connector types")
    public ResponseEntity<ApiResponse<List<VehicleCatalogEntry>>> getCatalog() {
        return ResponseEntity.ok(ApiResponse.<List<VehicleCatalogEntry>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(CATALOG)
                .build());
    }
}
