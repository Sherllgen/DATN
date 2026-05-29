package com.project.evgo.sharedkernel.events;

/**
 * Domain event published whenever a review is created, updated, or deleted.
 * @param stationId the station whose review aggregate has changed
 */
public record ReviewPostedEvent(Long stationId) {
}
