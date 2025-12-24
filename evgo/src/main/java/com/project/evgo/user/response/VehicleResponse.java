package com.project.evgo.user.response;

import com.project.evgo.sharedkernel.enums.ConnectorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Response DTO for Vehicle entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private Long id;
    private Long userId;
    private String brand;
    private String modelName;
    private Set<ConnectorType> connectorTypes;
    private Boolean inUse;
}
