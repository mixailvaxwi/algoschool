import { create } from 'zustand';

interface UserProfile {
    id: number;
    username: string;
    email: string;
    role: string;
    totalXp: number;
    balance: number;
}

interface AuthState {
    user: UserProfile | null;
    isAuthenticated: boolean;
    login: (token: string, user: UserProfile) => void;
    logout: () => void;
    updateBalance: (newBalance: number) => void;
    updateXp: (xpToAdd: number) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    // Теперь при загрузке страницы мы достаем профиль из памяти браузера
    user: JSON.parse(localStorage.getItem('user_profile') || 'null'),
    isAuthenticated: !!localStorage.getItem('jwt_token'),

    login: (token, user) => {
        localStorage.setItem('jwt_token', token);
        // Сохраняем профиль (с ролью ADMIN), чтобы он пережил F5
        localStorage.setItem('user_profile', JSON.stringify(user));
        set({ user, isAuthenticated: true });
    },

    logout: () => {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_profile'); // Не забываем удалять при выходе
        set({ user: null, isAuthenticated: false });
    },

    updateBalance: (newBalance) => set((state) => {
        if (!state.user) return state;
        const updatedUser = { ...state.user, balance: newBalance };
        localStorage.setItem('user_profile', JSON.stringify(updatedUser));
        return { user: updatedUser };
    }),

    updateXp: (xpToAdd) => set((state) => {
        if (!state.user) return state;
        const updatedUser = { ...state.user, totalXp: state.user.totalXp + xpToAdd };
        localStorage.setItem('user_profile', JSON.stringify(updatedUser));
        return { user: updatedUser };
    })
}));