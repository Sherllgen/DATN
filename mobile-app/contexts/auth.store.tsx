import { create } from "zustand";
import * as SecureStore from "expo-secure-store";
import { useUserStore } from "./user.store";

// SecureStore key constants
export const SECURE_KEY_ACCESS_TOKEN = "evgo_access_token";
export const SECURE_KEY_REFRESH_TOKEN = "evgo_refresh_token";

type AuthState = {
    accessToken: string | null;
    refreshToken: string | null;
    setAccessToken: (t: string | null) => void;
    /**
     * Persist both tokens to SecureStore and update in-memory state atomically.
     */
    saveTokens: (accessToken: string, refreshToken: string) => Promise<void>;
    /**
     * Clear all auth state from memory and SecureStore.
     */
    logout: () => Promise<void>;
};

export const useAuthStore = create<AuthState>((set) => ({
    accessToken: null,
    refreshToken: null,

    setAccessToken: (t) => set({ accessToken: t }),

    saveTokens: async (accessToken: string, refreshToken: string) => {
        await Promise.all([
            SecureStore.setItemAsync(SECURE_KEY_ACCESS_TOKEN, accessToken),
            SecureStore.setItemAsync(SECURE_KEY_REFRESH_TOKEN, refreshToken),
        ]);
        set({ accessToken, refreshToken });
    },

    logout: async () => {
        // Clear SecureStore first (fire-and-forget on individual failures)
        await Promise.allSettled([
            SecureStore.deleteItemAsync(SECURE_KEY_ACCESS_TOKEN),
            SecureStore.deleteItemAsync(SECURE_KEY_REFRESH_TOKEN),
        ]);
        set({ accessToken: null, refreshToken: null });
        useUserStore.getState().setUser(null);
    },
}));
