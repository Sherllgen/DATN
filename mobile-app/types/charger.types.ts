export enum ChargerStatus {
    AVAILABLE = 'AVAILABLE',
    OCCUPIED = 'OCCUPIED',
    OUT_OF_ORDER = 'OUT_OF_ORDER',
    OFFLINE = 'OFFLINE',
    MAINTENANCE = 'MAINTENANCE'
}

export enum PortStatus {
    AVAILABLE = 'AVAILABLE',
    OCCUPIED = 'OCCUPIED',
    FAULTED = 'FAULTED',
    UNAVAILABLE = 'UNAVAILABLE'
}

export interface PortResponse {
    id: number;
    portNumber: number;
    status: PortStatus;
    chargerId: number;
    createdAt: string;
}

export interface ChargerResponse {
    id: number;
    name: string;
    maxPower: number;
    connectorType: string;
    status: ChargerStatus;
    stationId: number;
    ports: PortResponse[];
    totalPorts: number;
    availablePorts: number;
    chargePointVendor?: string;
    chargePointModel?: string;
    chargePointSerial?: string;
    firmwareVersion?: string;
    lastHeartbeat?: string;
    createdAt: string;
}
