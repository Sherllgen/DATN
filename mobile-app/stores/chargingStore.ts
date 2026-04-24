import { create } from "zustand";
import { ChargingSessionResponse, ChargingMonitorData } from "@/types/charging.types";

interface ChargingState {
    activeSession: ChargingSessionResponse | null;
    lastMonitorData: ChargingMonitorData | null;
    setActiveSession: (session: ChargingSessionResponse | null) => void;
    setLastMonitorData: (data: ChargingMonitorData) => void;
    clearMonitorData: () => void;
    clearSession: () => void;
}

export const useChargingStore = create<ChargingState>((set) => ({
    activeSession: null,
    lastMonitorData: null,
    setActiveSession: (session) => set({ activeSession: session }),
    setLastMonitorData: (data) => set({ lastMonitorData: data }),
    clearMonitorData: () => set({ lastMonitorData: null }),
    clearSession: () => set({ activeSession: null, lastMonitorData: null }),
}));
