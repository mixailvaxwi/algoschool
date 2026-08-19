import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { apiClient } from '../../api/axios';
import { useAuthStore } from '../../store/authStore';
import { LogIn } from 'lucide-react';

export const LoginPage = () => {
    const navigate = useNavigate();
    const login = useAuthStore((state) => state.login);
    const [formData, setFormData] = useState({ username: '', password: '' });
    const [error, setError] = useState('');
    const [isLoading, setIsLoading] = useState(false);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError('');

        try {
            const response = await apiClient.post('/auth/login', formData);

            // ИСПРАВЛЕНИЕ 1: Прямо достаем token и user из объекта data
            const { token, user } = response.data;

            if (token && user) {
                // Теперь в стор летит правильный, плоский объект пользователя
                login(token, user);

                // ИСПРАВЛЕНИЕ 2: Проверяем роль с префиксом ROLE_
                if (user.role === 'ROLE_TEACHER') {
                    navigate('/teacher/courses');
                } else {
                    navigate('/courses');
                }
            } else {
                setError('Сервер не вернул токен или данные пользователя');
            }
        } catch (err: any) {
            setError(err.response?.data?.message || 'Неверный логин или пароль');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="min-h-[calc(100vh-64px)] flex items-center justify-center bg-slate-50 p-4 py-12">
            <div className="max-w-md w-full bg-white rounded-2xl shadow-sm border border-slate-100 p-8">
                <div className="flex flex-col items-center mb-8">
                    <div className="bg-blue-100 p-3 rounded-full mb-4 text-blue-600">
                        <LogIn size={32} />
                    </div>
                    <h1 className="text-2xl font-bold text-slate-800">С возвращением!</h1>
                    <p className="text-slate-500 mt-2">Войдите, чтобы продолжить обучение</p>
                </div>

                {error && <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-xl text-sm border border-red-100">{error}</div>}

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Логин</label>
                        <input
                            type="text"
                            required
                            value={formData.username}
                            onChange={(e) => setFormData({...formData, username: e.target.value})}
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                            placeholder="student@example.com"
                        />
                    </div>
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Пароль</label>
                        <input
                            type="password"
                            required
                            value={formData.password}
                            onChange={(e) => setFormData({...formData, password: e.target.value})}
                            className="w-full px-4 py-3 rounded-xl border border-slate-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-200 outline-none transition-all"
                            placeholder="••••••••"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={isLoading}
                        className="w-full py-3 px-4 bg-slate-800 hover:bg-slate-900 text-white font-medium rounded-xl transition-colors disabled:opacity-70 mt-4"
                    >
                        {isLoading ? 'Вход...' : 'Войти'}
                    </button>
                </form>

                <div className="mt-8 text-center text-sm text-slate-600">
                    Нет аккаунта? <Link to="/register" className="text-blue-600 hover:underline font-medium">Зарегистрироваться</Link>
                </div>
            </div>
        </div>
    );
};