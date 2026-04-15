import { create } from "zustand";
import { ChargingSessionResponse } from "@/types/charging.types";

interface ChargingState {
    activeSession: ChargingSessionResponse | null;
    setActiveSession: (session: ChargingSessionResponse | null) => void;
    clearSession: () => void;
}

export const useChargingStore = create<ChargingState>((set) => ({
    activeSession: null,
    setActiveSession: (session) => set({ activeSession: session }),
    clearSession: () => set({ activeSession: null }),
}));
