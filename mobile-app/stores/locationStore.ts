import { create } from 'zustand';
import * as Location from 'expo-location';

interface LocationStore {
    location: Location.LocationObject | null;
    setLocation: (location: Location.LocationObject | null) => void;
}

/**
 * Global location store shared across all screens.
 * This allows location to be accessed anywhere in the app without prop drilling.
 * 
 * Usage:
 * - Home screen: Updates location when user grants permission
 * - Map screen: Updates location when user grants permission OR when "locate" button is pressed
 * - Both screens can read the shared location state
 */
export const useLocationStore = create<LocationStore>((set) => ({
    location: null,
    setLocation: (location) => set({ location }),
}));
