import { create } from "zustand";

type User = {
    id: string;
    fullName: string;
    email: string;
    phone?: string;
    gender?: string;
    birthday?: string;
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
