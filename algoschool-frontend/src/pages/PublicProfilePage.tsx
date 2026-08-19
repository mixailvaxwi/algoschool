// @ts-ignore
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/axios';
import { User, Shield, ArrowLeft } from 'lucide-react';

interface PublicProfile {
    id: number;
    username: string;
    name: string;
    role: string;
}

export const PublicProfilePage = () => {
    // Получаем username из URL (например, /users/ivan_dev)
    const { username } = useParams<{ username: string }>();
    const navigate = useNavigate();

    const [profile, setProfile] = useState<PublicProfile | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await apiClient.get(`/users/${username}`);
                setProfile(response.data);
            } catch (err: any) {
                setError(err.response?.data?.message || 'Пользователь не найден');
            } finally {
                setIsLoading(false);
            }
        };
        if (username) fetchProfile();
    }, [username]);

    if (isLoading) return <div className="text-center py-20 text-slate-500">Загрузка профиля...</div>;
    if (error || !profile) return <div className="text-center py-20 text-red-500">{error}</div>;

    const roleText = profile.role === 'ROLE_TEACHER' ? 'Преподаватель' : 'Студент';
    const roleColor = profile.role === 'ROLE_TEACHER' ? 'bg-emerald-100 text-emerald-700' : 'bg-blue-100 text-blue-700';

    return (
        <div className="max-w-2xl mx-auto py-10 px-4">
            <button
                onClick={() => navigate(-1)}
                className="flex items-center gap-2 text-slate-500 mb-6 hover:text-slate-800 transition-colors"
            >
                <ArrowLeft size={18} /> Назад
            </button>

            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden text-center p-10">
                <div className="w-24 h-24 bg-slate-100 text-slate-400 rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm border-4 border-white ring-4 ring-slate-50">
                    <User size={48} />
                </div>

                {/* Если имя не указано, показываем только никнейм */}
                <h1 className="text-3xl font-bold text-slate-800">
                    {profile.name || profile.username}
                </h1>

                <p className="text-slate-500 mt-1 mb-4 font-medium">
                    @{profile.username}
                </p>

                <span className={`inline-flex items-center gap-1 px-4 py-1.5 rounded-full text-sm font-bold ${roleColor}`}>
                    <Shield size={16} /> {roleText}
                </span>

                <div className="mt-8 pt-8 border-t border-slate-100 text-slate-500 text-sm">
                    Это публичный профиль пользователя. В будущем здесь могут отображаться
                    достижения, статистика решений и пройденные курсы.
                </div>
            </div>
        </div>
    );
};