import { create } from 'zustand';
import { Station } from '@/types/station.types';

interface CachedStation {
    data: Station;
    timestamp: number;
}

interface StationCacheState {
    cache: Map<number, CachedStation>;
    ttl: number; // Time-to-live in milliseconds (default: 5 minutes)

    // Actions
    setStation: (station: Station) => void;
    getStation: (id: number) => Station | null;
    clearStation: (id: number) => void;
    clearAll: () => void;
    isStale: (id: number) => boolean;
}

const DEFAULT_TTL = 5 * 60 * 1000; // 5 minutes

export const useStationCache = create<StationCacheState>((set, get) => ({
    cache: new Map(),
    ttl: DEFAULT_TTL,

    setStation: (station: Station) => {
        const { cache } = get();
        const newCache = new Map(cache);
        newCache.set(station.id, {
            data: station,
            timestamp: Date.now(),
        });
        set({ cache: newCache });

        if (__DEV__) {
            console.log(`[StationCache] Set: Station #${station.id} (${station.name})`);
        }
    },

    getStation: (id: number) => {
        const { cache, ttl, isStale } = get();
        const cached = cache.get(id);

        if (!cached) {
            if (__DEV__) {
                console.log(`[StationCache] Miss: Station #${id}`);
            }
            return null;
        }

        // Check if expired
        if (isStale(id)) {
            if (__DEV__) {
                console.log(`[StationCache] Expired: Station #${id} (age: ${Date.now() - cached.timestamp}ms)`);
            }
            // Auto-cleanup expired entry
            get().clearStation(id);
            return null;
        }

        if (__DEV__) {
            console.log(`[StationCache] Hit: Station #${id} (age: ${Date.now() - cached.timestamp}ms)`);
        }

        return cached.data;
    },

    isStale: (id: number) => {
        const { cache, ttl } = get();
        const cached = cache.get(id);

        if (!cached) return true;

        const age = Date.now() - cached.timestamp;
        return age > ttl;
    },

    clearStation: (id: number) => {
        const { cache } = get();
        const newCache = new Map(cache);
        newCache.delete(id);
        set({ cache: newCache });

        if (__DEV__) {
            console.log(`[StationCache] Cleared: Station #${id}`);
        }
    },

    clearAll: () => {
        set({ cache: new Map() });

        if (__DEV__) {
            console.log('[StationCache] Cleared all entries');
        }
    },
}));
