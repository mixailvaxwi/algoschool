import React, { useState, useEffect } from 'react';
import { apiClient } from '../api/axios';
import { useAuthStore } from '../store/authStore';
import {User, Mail, Shield, Save, Loader2, CheckCircle} from 'lucide-react';

interface ProfileData {
    name: string;
    email: string;
    username: string;
    role: string;
}

export const ProfilePage = () => {
    // @ts-ignore
    const { user, login } = useAuthStore();
    const [profile, setProfile] = useState<ProfileData>({ name: '', email: '', username: '', role: '' });

    const [isLoading, setIsLoading] = useState(true);
    const [isSaving, setIsSaving] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await apiClient.get('/users/me');
                setProfile(response.data);
            } catch (err) {
                setError('Не удалось загрузить профиль');
            } finally {
                setIsLoading(false);
            }
        };
        fetchProfile();
    }, []);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSaving(true);
        setError('');
        setSuccessMessage('');

        try {
            const response = await apiClient.put('/users/me', {
                name: profile.name,
                email: profile.email
            });

            // Обновляем локальный стейт
            setProfile(response.data);

            // Обновляем данные пользователя в Zustand Store (чтобы обновился Navbar)
            const token = localStorage.getItem('token');
            if (token) login(token, response.data);

            setSuccessMessage('Профиль успешно обновлен!');
            setTimeout(() => setSuccessMessage(''), 3000);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Ошибка при сохранении профиля');
        } finally {
            setIsLoading(false);
        }
    };

    if (isLoading) return <div className="text-center py-20 text-slate-500">Загрузка профиля...</div>;

    const roleText = profile.role === 'ROLE_TEACHER' ? 'Преподаватель' : 'Студент';
    const roleColor = profile.role === 'ROLE_TEACHER' ? 'bg-emerald-100 text-emerald-700' : 'bg-blue-100 text-blue-700';

    return (
        <div className="max-w-2xl mx-auto py-10">
            <h1 className="text-3xl font-bold text-slate-800 mb-8">Личный кабинет</h1>

            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <div className="bg-slate-50 p-8 border-b border-slate-200 flex items-center gap-6">
                    <div className="bg-blue-600 text-white p-4 rounded-full shadow-md">
                        <User size={40} />
                    </div>
                    <div>
                        <h2 className="text-2xl font-bold text-slate-800">{profile.username}</h2>
                        <span className={`inline-flex items-center gap-1 mt-1 px-3 py-1 rounded-full text-xs font-bold ${roleColor}`}>
                            <Shield size={12} /> {roleText}
                        </span>
                    </div>
                </div>

                <div className="p-8">
                    {error && <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-xl text-sm border border-red-100">{error}</div>}
                    {successMessage && <div className="mb-6 p-4 bg-emerald-50 text-emerald-700 rounded-xl text-sm border border-emerald-100 flex items-center gap-2"><CheckCircle size={18}/> {successMessage}</div>}

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Имя и Фамилия</label>
                            <div className="relative">
                                <User className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                <input
                                    type="text"
                                    required
                                    value={profile.name}
                                    onChange={(e) => setProfile({...profile, name: e.target.value})}
                                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all focus:bg-white"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Email адрес</label>
                            <div className="relative">
                                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" size={18} />
                                <input
                                    type="email"
                                    required
                                    value={profile.email}
                                    onChange={(e) => setProfile({...profile, email: e.target.value})}
                                    className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none transition-all focus:bg-white"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-400 mb-1">Логин (нельзя изменить)</label>
                            <input
                                type="text"
                                disabled
                                value={profile.username}
                                className="w-full px-4 py-3 bg-slate-100 border border-slate-200 rounded-xl text-slate-500 cursor-not-allowed"
                            />
                        </div>

                        <div className="pt-4 flex justify-end">
                            <button
                                type="submit"
                                disabled={isSaving}
                                className="flex items-center gap-2 px-8 py-3 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-xl transition-colors disabled:opacity-70"
                            >
                                {isSaving ? <><Loader2 className="animate-spin" size={18} /> Сохранение...</> : <><Save size={18} /> Сохранить изменения</>}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};