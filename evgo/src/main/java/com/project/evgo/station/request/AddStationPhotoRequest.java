package com.project.evgo.station.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for adding a photo to a station.
 * Public API - accessible by other modules.
 */
public record AddStationPhotoRequest(

	@NotBlank(message = "Image URL is required") 
	String imageUrl,

	String caption,

	Integer displayOrder){
}
