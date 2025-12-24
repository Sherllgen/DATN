import { create } from "zustand";

type User = {
    id: string;
    name: string;
    email: string;
    avatarUrl?: string;
};

type UserState = {
    user: User | null;
    setUser: (u: User | null) => void;
};

export const useUserStore = create<UserState>((set) => ({
    user: null,
    setUser: (u: User | null) => set({ user: u }),
}));
