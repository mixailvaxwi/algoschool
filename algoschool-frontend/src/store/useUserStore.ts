import { create } from 'zustand';

interface UserState {
    name: string;
    role: string | null;
    setUserData: (name: string, role: string) => void;
    clearUser: () => void;
}

export const useUserStore = create<UserState>((set) => ({
    name: '',
    role: null,
    setUserData: (name, role) => set({ name, role }),
    clearUser: () => set({ name: '', role: null })
}));