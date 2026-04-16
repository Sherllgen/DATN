export enum ChargingSessionStatus {
    PREPARING = "PREPARING",
    CHARGING = "CHARGING",
    SUSPENDED_EV = "SUSPENDED_EV",
    SUSPENDED_EVSE = "SUSPENDED_EVSE",
    FINISHING = "FINISHING",
    FAULTED = "FAULTED",
    COMPLETED = "COMPLETED",
    INTERRUPTED = "INTERRUPTED"
}

export interface StartChargingRequest {
    portId: number;
    bookingId?: number;
}

export interface StopChargingRequest {
    sessionId: number;
}

export interface ChargingSessionResponse {
    id: number;
    userId: number;
    portId: number;
    bookingId: number | null;
    invoiceId: number | null;
    transactionId: number | null;
    startTime: string | null;
    endTime: string | null;
    totalKwh: number | null;
    status: ChargingSessionStatus;
    createdAt: string;
    updatedAt: string;
}

export interface ChargingMonitorData {
    status: ChargingSessionStatus;
    consumedKwh: number;
    estimatedCost: number;
    currentMeterValue: number | null;
    chargingRatePerKwh: number;
    timestamp: string;
}
