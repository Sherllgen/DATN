package com.project.evgo.charger;

/**
 * Event published when a charge point completes a BootNotification.
 * Other modules can listen for this to react to charge points coming online.
 */
public record ChargePointBootedEvent(Long chargerId) {
}
