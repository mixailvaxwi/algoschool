import { create } from 'zustand';

interface UserState {
    name: string;
    totalXp: number;
    coins: number;
    role: string | null; // <-- Добавили роль

    // Обновили сигнатуру: теперь принимаем role
    setUserData: (name: string, xp: number, coins: number, role: string) => void;
    addTotalXp: (amount: number) => void;
    addCoins: (amount: number) => void;
    clearUser: () => void;
}

export const useUserStore = create<UserState>((set) => ({
    name: '',
    totalXp: 0,
    coins: 0,
    role: null, // Изначально роли нет

    // Сохраняем роль вместе с остальными данными
    setUserData: (name, xp, coins, role) => set({ name, totalXp: xp, coins, role }),

    addTotalXp: (amount) => set((state) => ({ totalXp: state.totalXp + amount })),
    addCoins: (amount) => set((state) => ({ coins: state.coins + amount })),

    // При логауте затираем всё
    clearUser: () => set({ name: '', totalXp: 0, coins: 0, role: null })
}));