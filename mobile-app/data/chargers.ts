export type ConnectorType =
    | "SOCKET_220V"
    | "CCS1"
    | "CCS2"
    | "CHADEMO"
    | "TYPE2"
    | "J1772";

export type ChargerStatus = "AVAILABLE" | "OCCUPIED" | "FAULTED" | "UNAVAILABLE";

export interface ChargerPort {
    id: number;
    portNumber: number;
    status: ChargerStatus;
    chargerId: number;
    createdAt: string;
}

export interface Charger {
    id: number;
    name: string;
    maxPower: number;
    connectorType: ConnectorType;
    status: ChargerStatus;
    stationId: number;
    ports: ChargerPort[];
    totalPorts: number;
    availablePorts: number;
    chargePointVendor: string;
    chargePointModel: string;
    chargePointSerial: string;
    firmwareVersion: string;
    lastHeartbeat: string;
    createdAt: string;
}

export interface ChargersApiResponse {
    status: number;
    message: string;
    data: Charger[];
}

export const mockChargersResponse: ChargersApiResponse = {
    status: 0,
    message: "Success",
    data: [
        {
            id: 1,
            name: "Tesla (CCS2)",
            maxPower: 100,
            connectorType: "CCS2",
            status: "AVAILABLE",
            stationId: 1,
            totalPorts: 2,
            availablePorts: 2,
            chargePointVendor: "Tesla",
            chargePointModel: "Wall Connector",
            chargePointSerial: "TS-001",
            firmwareVersion: "1.0.3",
            lastHeartbeat: "2026-03-10T17:00:00.000Z",
            createdAt: "2026-01-01T00:00:00.000Z",
            ports: [
                { id: 1, portNumber: 1, status: "AVAILABLE", chargerId: 1, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 2, portNumber: 2, status: "AVAILABLE", chargerId: 1, createdAt: "2026-01-01T00:00:00.000Z" },
            ],
        },
        {
            id: 2,
            name: "Mennekes (Type 2)",
            maxPower: 50,
            connectorType: "TYPE2",
            status: "AVAILABLE",
            stationId: 1,
            totalPorts: 2,
            availablePorts: 1,
            chargePointVendor: "Mennekes",
            chargePointModel: "AMTRON",
            chargePointSerial: "MN-002",
            firmwareVersion: "2.1.0",
            lastHeartbeat: "2026-03-10T17:00:00.000Z",
            createdAt: "2026-01-01T00:00:00.000Z",
            ports: [
                { id: 3, portNumber: 1, status: "AVAILABLE", chargerId: 2, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 4, portNumber: 2, status: "OCCUPIED",  chargerId: 2, createdAt: "2026-01-01T00:00:00.000Z" },
            ],
        },
        {
            id: 3,
            name: "CHAdeMO",
            maxPower: 100,
            connectorType: "CHADEMO",
            status: "AVAILABLE",
            stationId: 1,
            totalPorts: 1,
            availablePorts: 1,
            chargePointVendor: "ABB",
            chargePointModel: "Terra 54",
            chargePointSerial: "AB-003",
            firmwareVersion: "3.0.1",
            lastHeartbeat: "2026-03-10T17:00:00.000Z",
            createdAt: "2026-01-01T00:00:00.000Z",
            ports: [
                { id: 5, portNumber: 1, status: "AVAILABLE", chargerId: 3, createdAt: "2026-01-01T00:00:00.000Z" },
            ],
        },
        {
            id: 4,
            name: "CCS1",
            maxPower: 50,
            connectorType: "CCS1",
            status: "AVAILABLE",
            stationId: 1,
            totalPorts: 2,
            availablePorts: 2,
            chargePointVendor: "BTC Power",
            chargePointModel: "DCFC-60",
            chargePointSerial: "BT-004",
            firmwareVersion: "1.2.0",
            lastHeartbeat: "2026-03-10T17:00:00.000Z",
            createdAt: "2026-01-01T00:00:00.000Z",
            ports: [
                { id: 6, portNumber: 1, status: "AVAILABLE", chargerId: 4, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 7, portNumber: 2, status: "AVAILABLE", chargerId: 4, createdAt: "2026-01-01T00:00:00.000Z" },
            ],
        },
        {
            id: 5,
            name: "J1772 (Type 1)",
            maxPower: 22,
            connectorType: "J1772",
            status: "AVAILABLE",
            stationId: 1,
            totalPorts: 3,
            availablePorts: 2,
            chargePointVendor: "Leviton",
            chargePointModel: "EVB32",
            chargePointSerial: "LV-005",
            firmwareVersion: "2.0.0",
            lastHeartbeat: "2026-03-10T17:00:00.000Z",
            createdAt: "2026-01-01T00:00:00.000Z",
            ports: [
                { id: 8,  portNumber: 1, status: "AVAILABLE", chargerId: 5, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 9,  portNumber: 2, status: "AVAILABLE", chargerId: 5, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 10, portNumber: 3, status: "OCCUPIED",  chargerId: 5, createdAt: "2026-01-01T00:00:00.000Z" },
            ],
        },
        {
            id: 6,
            name: "Socket 220V",
            maxPower: 3,
            connectorType: "SOCKET_220V",
            status: "AVAILABLE",
            stationId: 1,
            totalPorts: 4,
            availablePorts: 4,
            chargePointVendor: "Generic",
            chargePointModel: "Basic-AC",
            chargePointSerial: "GN-006",
            firmwareVersion: "1.0.0",
            lastHeartbeat: "2026-03-10T17:00:00.000Z",
            createdAt: "2026-01-01T00:00:00.000Z",
            ports: [
                { id: 11, portNumber: 1, status: "AVAILABLE", chargerId: 6, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 12, portNumber: 2, status: "AVAILABLE", chargerId: 6, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 13, portNumber: 3, status: "AVAILABLE", chargerId: 6, createdAt: "2026-01-01T00:00:00.000Z" },
                { id: 14, portNumber: 4, status: "AVAILABLE", chargerId: 6, createdAt: "2026-01-01T00:00:00.000Z" },
            ],
        },
    ],
};
