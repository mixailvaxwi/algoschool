import { useState, useEffect, useCallback } from 'react';
import { apiClient } from '../../api/axios';
import { useAuthStore } from '../../store/authStore';
import { Search, ShieldCheck, Lock, Unlock, KeyRound, Copy, Check, X } from 'lucide-react';

type Role = 'ROLE_STUDENT' | 'ROLE_TEACHER' | 'ROLE_ADMIN';

interface AdminUser {
    id: number;
    username: string;
    email: string;
    name: string | null;
    role: Role;
    locked: boolean;
}

const ROLE_LABELS: Record<Role, string> = {
    ROLE_STUDENT: 'Студент',
    ROLE_TEACHER: 'Преподаватель',
    ROLE_ADMIN: 'Администратор',
};

export const AdminUsersPage = () => {
    const currentUsername = useAuthStore((state) => state.user?.username);

    const [users, setUsers] = useState<AdminUser[]>([]);
    const [isLoading, setIsLoading] = useState(true);
    const [query, setQuery] = useState('');
    const [busyUserId, setBusyUserId] = useState<number | null>(null);
    const [generatedPassword, setGeneratedPassword] = useState<{ username: string; password: string } | null>(null);

    const fetchUsers = useCallback(async (q: string) => {
        setIsLoading(true);
        try {
            const response = await apiClient.get<AdminUser[]>('/admin/users', { params: q ? { q } : {} });
            setUsers(response.data);
        } catch (error) {
            console.error('Ошибка загрузки пользователей', error);
        } finally {
            setIsLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUsers('');
    }, [fetchUsers]);

    const handleSearchSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        fetchUsers(query);
    };

    const handleRoleChange = async (user: AdminUser, role: Role) => {
        setBusyUserId(user.id);
        try {
            const response = await apiClient.put<AdminUser>(`/admin/users/${user.id}/role`, { role });
            setUsers((prev) => prev.map((u) => (u.id === user.id ? response.data : u)));
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось изменить роль');
        } finally {
            setBusyUserId(null);
        }
    };

    const handleToggleLock = async (user: AdminUser) => {
        const action = user.locked ? 'unblock' : 'block';
        if (!user.locked && !window.confirm(`Заблокировать пользователя ${user.username}? Он не сможет войти.`)) return;

        setBusyUserId(user.id);
        try {
            const response = await apiClient.post<AdminUser>(`/admin/users/${user.id}/${action}`);
            setUsers((prev) => prev.map((u) => (u.id === user.id ? response.data : u)));
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось изменить статус блокировки');
        } finally {
            setBusyUserId(null);
        }
    };

    const handleResetPassword = async (user: AdminUser) => {
        if (!window.confirm(`Сбросить пароль пользователя ${user.username}? Старый пароль перестанет работать немедленно.`)) return;

        setBusyUserId(user.id);
        try {
            const response = await apiClient.post<{ newPassword: string }>(`/admin/users/${user.id}/reset-password`);
            setGeneratedPassword({ username: user.username, password: response.data.newPassword });
        } catch (error: any) {
            alert(error.response?.data?.message || 'Не удалось сбросить пароль');
        } finally {
            setBusyUserId(null);
        }
    };

    return (
        <div>
            <div className="flex items-center gap-3 mb-6">
                <ShieldCheck className="text-blue-600" size={24} />
                <h1 className="text-2xl font-bold text-slate-800">Пользователи</h1>
            </div>

            <form onSubmit={handleSearchSubmit} className="flex gap-2 mb-6">
                <div className="relative flex-1 max-w-sm">
                    <Search className="absolute left-3 top-3 text-slate-400" size={18} />
                    <input
                        type="text"
                        value={query}
                        onChange={(e) => setQuery(e.target.value)}
                        placeholder="Логин, email или имя"
                        className="w-full pl-10 pr-3 py-2 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-blue-500"
                    />
                </div>
                <button
                    type="submit"
                    className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors"
                >
                    Найти
                </button>
            </form>

            {isLoading ? (
                <div className="text-slate-400 text-sm py-8 text-center">Загрузка...</div>
            ) : users.length === 0 ? (
                <div className="bg-slate-50 p-8 rounded-xl text-center text-slate-500 border border-slate-200">
                    Никого не нашлось.
                </div>
            ) : (
                <div className="overflow-x-auto border border-slate-200 rounded-xl">
                    <table className="w-full text-sm">
                        <thead>
                            <tr className="bg-slate-50 text-left text-slate-500 border-b border-slate-200">
                                <th className="px-4 py-3 font-semibold">Логин</th>
                                <th className="px-4 py-3 font-semibold">Имя</th>
                                <th className="px-4 py-3 font-semibold">Email</th>
                                <th className="px-4 py-3 font-semibold">Роль</th>
                                <th className="px-4 py-3 font-semibold">Статус</th>
                                <th className="px-4 py-3 font-semibold text-right">Действия</th>
                            </tr>
                        </thead>
                        <tbody>
                            {users.map((user) => {
                                const isSelf = user.username === currentUsername;
                                const isBusy = busyUserId === user.id;
                                return (
                                    <tr key={user.id} className="border-b border-slate-100 last:border-0">
                                        <td className="px-4 py-3 font-medium text-slate-800">
                                            {user.username} {isSelf && <span className="text-xs text-slate-400">(вы)</span>}
                                        </td>
                                        <td className="px-4 py-3 text-slate-600">{user.name || '—'}</td>
                                        <td className="px-4 py-3 text-slate-600">{user.email}</td>
                                        <td className="px-4 py-3">
                                            <select
                                                value={user.role}
                                                disabled={isBusy || (isSelf && user.role === 'ROLE_ADMIN')}
                                                onChange={(e) => handleRoleChange(user, e.target.value as Role)}
                                                className="border border-slate-200 rounded-lg px-2 py-1 outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-60 disabled:cursor-not-allowed bg-white"
                                            >
                                                {(Object.keys(ROLE_LABELS) as Role[]).map((role) => (
                                                    <option key={role} value={role}>{ROLE_LABELS[role]}</option>
                                                ))}
                                            </select>
                                        </td>
                                        <td className="px-4 py-3">
                                            {user.locked ? (
                                                <span className="bg-red-100 text-red-700 text-xs px-2 py-1 rounded-md">Заблокирован</span>
                                            ) : (
                                                <span className="bg-emerald-100 text-emerald-700 text-xs px-2 py-1 rounded-md">Активен</span>
                                            )}
                                        </td>
                                        <td className="px-4 py-3">
                                            <div className="flex justify-end gap-1">
                                                <button
                                                    onClick={() => handleResetPassword(user)}
                                                    disabled={isBusy}
                                                    title="Сбросить пароль"
                                                    className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors disabled:opacity-40"
                                                >
                                                    <KeyRound size={16} />
                                                </button>
                                                <button
                                                    onClick={() => handleToggleLock(user)}
                                                    disabled={isBusy || isSelf}
                                                    title={user.locked ? 'Разблокировать' : 'Заблокировать'}
                                                    className={`p-2 rounded-lg transition-colors disabled:opacity-40 ${
                                                        user.locked
                                                            ? 'text-emerald-500 hover:text-emerald-700 hover:bg-emerald-50'
                                                            : 'text-slate-400 hover:text-red-600 hover:bg-red-50'
                                                    }`}
                                                >
                                                    {user.locked ? <Unlock size={16} /> : <Lock size={16} />}
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            )}

            {generatedPassword && (
                <PasswordResetModal
                    username={generatedPassword.username}
                    password={generatedPassword.password}
                    onClose={() => setGeneratedPassword(null)}
                />
            )}
        </div>
    );
};

const PasswordResetModal = ({ username, password, onClose }: { username: string; password: string; onClose: () => void }) => {
    const [copied, setCopied] = useState(false);

    const handleCopy = async () => {
        try {
            await navigator.clipboard.writeText(password);
            setCopied(true);
        } catch {
            // Буфер обмена недоступен (нет разрешения, не https) — пароль всё равно виден на экране.
        }
    };

    return (
        <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-2xl shadow-xl max-w-md w-full p-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-bold text-slate-800">Пароль сброшен</h2>
                    <button onClick={onClose} className="text-slate-400 hover:text-slate-700">
                        <X size={20} />
                    </button>
                </div>
                <p className="text-sm text-slate-600 mb-4">
                    Новый пароль для <span className="font-semibold text-slate-800">{username}</span>.
                    Он показывается только один раз — передайте его пользователю сейчас.
                </p>
                <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl p-3 mb-4">
                    <code className="flex-1 font-mono text-base text-slate-800 select-all">{password}</code>
                    <button
                        onClick={handleCopy}
                        className="p-2 text-slate-500 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                        title="Скопировать"
                    >
                        {copied ? <Check size={18} className="text-emerald-600" /> : <Copy size={18} />}
                    </button>
                </div>
                <button
                    onClick={onClose}
                    className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors"
                >
                    Готово
                </button>
            </div>
        </div>
    );
};
