import { create } from "zustand";
import { useUserStore } from "./user.store";

type AuthState = {
    accessToken: string | null;
    setAccessToken: (t: string | null) => void;
    logout: () => void;
};

export const useAuthStore = create<AuthState>((set) => ({
    accessToken: null,
    setAccessToken: (t) => set({ accessToken: t }),
    logout: () => {
        set({ accessToken: null });
        useUserStore.getState().setUser(null);
    },
}));
