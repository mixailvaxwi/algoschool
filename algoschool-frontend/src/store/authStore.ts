import { create } from 'zustand';

interface UserProfile {
    id: number;
    username: string;
    email: string;
    role: string;
}

interface AuthState {
    user: UserProfile | null;
    isAuthenticated: boolean;
    login: (token: string, user: UserProfile) => void;
    logout: () => void;
}

/**
 * Читает профиль из localStorage.
 *
 * Обёрнуто в try/catch намеренно: раньше голый JSON.parse выполнялся на уровне
 * модуля, и любая испорченная запись (оборванная запись, ручная правка в
 * DevTools, старый формат) роняла приложение белым экраном ещё до отрисовки.
 * Теперь битые данные просто чистятся, и пользователь видит экран входа.
 */
const readStoredUser = (): UserProfile | null => {
    const raw = localStorage.getItem('user_profile');
    if (!raw) return null;

    try {
        return JSON.parse(raw) as UserProfile;
    } catch {
        localStorage.removeItem('user_profile');
        localStorage.removeItem('token');
        return null;
    }
};

const storedUser = readStoredUser();

export const useAuthStore = create<AuthState>((set) => ({
    user: storedUser,
    // Считаем вошедшим только когда есть и токен, и профиль: иначе Navbar и
    // TeacherRoute получают isAuthenticated === true при user === null.
    isAuthenticated: !!localStorage.getItem('token') && storedUser !== null,

    login: (token, user) => {
        localStorage.setItem('token', token);
        localStorage.setItem('user_profile', JSON.stringify(user));
        set({ user, isAuthenticated: true });
    },

    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user_profile');
        set({ user: null, isAuthenticated: false });
    }
}));
