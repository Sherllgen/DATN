export interface ActiveChargingSession {
    kwh: number;
    time: string;
    percentage: number;
    current: number; // Amp
    fees: number;    // $
}

export const mockChargingSession: ActiveChargingSession = {
    kwh: 30,
    time: "00:14:55",
    percentage: 30,
    current: 12.24,
    fees: 4.27,
};

export interface ChargingProcessData {
    status: "CHARGING" | "COMPLETED" | "STOPPED" | "STARTING";
    sessionId: string;
    batteryPercent: number;
    powerKw: number;
    energyKwh: number;
    durationSeconds: number;
    totalFees: number;
    currentAmp: number;
    remainingMinutes?: number;
}

export const mockChargingProcessData: ChargingProcessData = {
    // status: "COMPLETED",
    status: "CHARGING",
    sessionId: "EV-999",
    batteryPercent: 59,
    powerKw: 140.5,
    energyKwh: 55.2,
    durationSeconds: 895,
    totalFees: 4.27,
    currentAmp: 12.24,
    remainingMinutes: 30
};
