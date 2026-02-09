package com.project.evgo.station;

/**
 * Value object for port counts.
 */
public record PortCounts(int totalPorts, int availablePorts) {
    public static final PortCounts ZERO = new PortCounts(0, 0);
}
